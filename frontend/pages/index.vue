<template>
  <div class="Login">
    <BaseHeadline
      title="Sign in to myTurk"
      description="The true method of knowledge is experiment. — William Blake"
    />
    <Container :items="login" @login="handleLogin" />
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

import BaseHeadline from '@/components/BaseHeadline.vue'
import Container from '@/components/login/Container.vue'
import api from '@/api'
import { APIRes, Credentials } from '@/lib/types'

type IndexData = {
  login: string[]
}

export default Vue.extend({
  name: 'Login',
  components: {
    BaseHeadline,
    Container,
  },
  props: {
    loggedOut: Boolean,
  },
  data: (): IndexData => ({
    login: ['AWS Access Key ID', 'AWS Secret Access Key'],
  }),
  watch: {
    loggedOut: {
      immediate: true,
      handler(loggedOut: boolean): void {
        if (loggedOut) {
          localStorage.setItem('token', JSON.stringify(''))
          this.$toasted.success('you are logged out', {
            position: 'bottom-right',
            duration: 2000,
          })
        }
      },
    },
  },
  methods: {
    // TODO: write mixin functions
    async handleLogin(credentials: Credentials): Promise<void> {
      const res = await api.login(credentials) as APIRes
      console.log(res)
      if (res.success) {
        const apiRes = res as APIRes
        localStorage.setItem('token', JSON.stringify(apiRes.token))
        this.$toasted.success(apiRes.message, {
          position: 'bottom-right',
          duration: 1500,
        })
        setTimeout(() => {
          this.$router.push({ path: 'Overview' })
        }, 200)
      } else {
        this.$toasted.error(res.message, {
          position: 'bottom-right',
          duration: 3000,
        })
      }
    },
  },
})
</script>

<style lang="scss">
.Login {
  position: relative;
  width: 35%;
  min-width: 300px;
  max-width: 600px;
  display: flex;
  justify-content: center;
  height: 100%;
  flex-direction: column;
  transform: translateY(-10%);
}
</style>
