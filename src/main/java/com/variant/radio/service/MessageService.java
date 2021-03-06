package com.variant.radio.service;

import com.variant.radio.domain.Message;
import com.variant.radio.domain.User;
import com.variant.radio.domain.UserSubscription;
import com.variant.radio.domain.Views;
import com.variant.radio.dto.EventType;
import com.variant.radio.dto.MessagePageDto;
import com.variant.radio.dto.MetaDto;
import com.variant.radio.dto.ObjectType;
import com.variant.radio.repository.MessageRepository;
import com.variant.radio.repository.UserSubscriptionRepository;
import com.variant.radio.util.WsSender;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MessageService {
    private static  String URL_PATTERN = "https?:\\/\\/?[\\w\\d\\._\\-%\\/\\?=&#]+";
    private static  String IMAGE_PATTERN = "\\.(jpeg|jpg|gif|png)$";

    private static  Pattern URL_REGEX = Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE);
    private static  Pattern IMG_REGEX = Pattern.compile(IMAGE_PATTERN, Pattern.CASE_INSENSITIVE);

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final MessageRepository messageRepository;
    private final BiConsumer<EventType, Message> wsSender;


    @Autowired
    public MessageService(UserSubscriptionRepository userSubscriptionRepository, MessageRepository messageRepository, WsSender wsSender) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.messageRepository = messageRepository;
        this.wsSender = wsSender.getSender(ObjectType.MESSAGE, Views.IdTextDate.class);
    }



    private void fillMeta(Message message) throws IOException {
        String text = message.getText();
        Matcher matcher = URL_REGEX.matcher(text);

        if (matcher.find()) {
            String url = text.substring(matcher.start(), matcher.end());

            matcher = IMG_REGEX.matcher(url);

            message.setLink(url);

            if (matcher.find()) {
                message.setLinkCover(url);
            } else if (!url.contains("youtu")) {
                MetaDto metaDto = getMeta(url);

                message.setLinkCover(metaDto.getCover());
                message.setLinkTitle(metaDto.getTitle());
                message.setLinkDescription(metaDto.getDescription());
            }
        }
    }

    private MetaDto getMeta(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements title = doc.select("meta[name$=title], meta[property$=title]");
        Elements description = doc.select("meta[name$=description], meta[property$=description]");
        Elements cover = doc.select("meta[name$=image], meta[property$=image]");
        return new MetaDto(
                getContent(title.first()),
                getContent(description.first()),
                getContent(cover.first())
        );

    }

    private String getContent(Element element) {
        return element == null ? "" : element.attr("content");
    }


    public void delete(Message message) {
        messageRepository.delete(message);
        wsSender.accept(EventType.DELETE, message);
    }


    public Message update(Message messageFromDB, Message message, User user) throws IOException {
        messageFromDB.setText(message.getText());
//        BeanUtils.copyProperties(message, messageFromDB, "id");
//        messageFromDB.setAuthor(user);
        fillMeta(messageFromDB);
        Message updatedMessage = messageRepository.save(messageFromDB);
        wsSender.accept(EventType.UPDATE, updatedMessage);
        return updatedMessage;
    }

    public Message create(Message message, User user) throws IOException {
        message.setDateOfCreation(LocalDateTime.now());
        fillMeta(message);
        message.setAuthor(user);
        Message savedMessage = messageRepository.save(message);
        wsSender.accept(EventType.CREATE, savedMessage);
        return savedMessage;
    }

    public MessagePageDto findForUser(Pageable pageable, User user) {
        List<User> channels = userSubscriptionRepository.findBySubscriber(user)
                .stream()
                .filter(UserSubscription::isActive)
                .map(UserSubscription::getChannel)
                .collect(Collectors.toList());

        channels.add(user);

        Page<Message> page = messageRepository.findByAuthorIn(channels, pageable);

        return new MessagePageDto(
                page.getContent(),
                pageable.getPageNumber(),
                page.getTotalPages());
    }
}
