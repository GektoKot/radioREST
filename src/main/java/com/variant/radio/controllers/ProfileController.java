package com.variant.radio.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.variant.radio.domain.User;
import com.variant.radio.domain.UserSubscription;
import com.variant.radio.domain.Views;
import com.variant.radio.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("profile")
public class ProfileController {
    private final ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("{id}")
    @JsonView(Views.IdTextProfile.class)
    public User get(@PathVariable("id") User user) {
        return user;
    }

    @PostMapping("change-subscription/{channelId}")
    @JsonView(Views.IdTextProfile.class)
    public User changeSubscription(@AuthenticationPrincipal User subscriber,
                                   @PathVariable("channelId") User channel) {
        if (subscriber.equals(channel)) {
            return channel;
        } else {
            return profileService.changeSubscription(subscriber, channel);
        }
    }
    @GetMapping("get-subscribers/{channelId}")
    @JsonView(Views.IdText.class)
    public List<UserSubscription> subscribers(@PathVariable("channelId") User channel) {
        return profileService.getSubscribers(channel);
    }
    @PostMapping("change-status/{subscriberId}")
    @JsonView(Views.IdText.class)
    public UserSubscription changeSubscriptionStatus(@AuthenticationPrincipal User channel,
                                                     @PathVariable("subscriberId") User subscriber) {
        return profileService.changeSubscriptionStatus(channel, subscriber);
    }

}
