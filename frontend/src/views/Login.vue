<template>
    <div class="Login">
        <BaseHeadline
                title="Sign in to myTurk"
                description="The true method of knowledge is experiment. — William Blake"
        />
        <Container :items="login" @login="handleLogin"/>
    </div>
</template>

<script>
    import BaseHeadline from '@/components/BaseHeadline.vue'
    import Container from '@/components/login/Container.vue'
    import api from '@/api'
    import utils from '@/utils'

    export default {
        name: 'Login',
        components: {
            BaseHeadline,
            Container,
        },
        props: {
            loggedOut: Boolean,
        },
        data: () => ({
            login: ['AWS Access Key ID', 'AWS Secret Access Key'],
        }),
        watch: {
            loggedOut: {
                immediate: true,
                handler: function (loggedOut) {
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
            //TODO: write mixin functions
            async handleLogin(credentials) {
                try {
                    let res = await api.login(credentials)
                    localStorage.setItem('token', JSON.stringify(res.token))
                    this.$toasted.success(res.message, {
                        position: 'bottom-right',
                        duration: 1500,
                    })
                    setTimeout(() => {
                        this.$router.push({path: 'overview'})
                    }, 200)
                } catch (errorResponse) {
                    let errorMessage = await utils.resolveErrorMessage(errorResponse);

                    this.$toasted.error(errorMessage, {
                        position: 'bottom-right',
                        duration: 3000,
                    })
                }
            },
        },
    }
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
