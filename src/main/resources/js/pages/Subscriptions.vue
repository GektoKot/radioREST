<template>
  <v-container>
    <v-layout align-content-space-around justify-center column>
      <v-list>
        <v-list-item v-for="item in subscriptions"
        :key="item.id">
          <user-link :user="item.subscriber">
          </user-link>
          <v-btn @click="changeSubscriptionStatus(item.subscriber.id)">
            {{item.active ? "Dismiss" : "Approve"}}
          </v-btn>
        </v-list-item>
      </v-list>
    </v-layout>
  </v-container>
</template>

<script>
import ProfileApi from 'js/api/profile.js'
import UserLink from 'js/components/UserLink.vue'
export default {
  name: 'Subscriptions',
  components: {UserLink},
  data() {
    return {
      subscriptions: []
    }
  },
  methods: {
    async changeSubscriptionStatus(subscriberId) {
      await ProfileApi.changeSubscriptionStatus(subscriberId)

      const subscriptionIndex = this.subscriptions.findIndex(item =>
          item.subscriber.id === subscriberId)
      const  subscription = this.subscriptions[subscriptionIndex]
      this.subscriptions = [
          ...this.subscriptions.slice(0, subscriptionIndex),
        {
          ...subscription,
          active: !subscription.active
        },
          ...this.subscriptions.slice(subscriptionIndex + 1)
      ]
    }
  },
  async beforeMount() {
    const resp = await ProfileApi.subscriberList(this.$store.state.profile.id)
    this.subscriptions = await resp.json()
  }
}
</script>

<style scoped>

</style>