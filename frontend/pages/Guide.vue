<template>
<div class="Guide">
    <BaseHeadline
      :style="{position: 'fixed', top: '0', paddingTop: '60px', borderBottom: '1px solid grey', paddingBottom: '40px', width: '100%', backgroundColor: '#F0F1F6', zIndex: '1'}"
      :route="route"
      description="Guide"
      title="myTurk"
      top="40px"
    />
    <div class="GuideWrapper">
        <div class="SideBar">
            <a href="#Experiment">Experiment</a>
            <a href="#HIT">HIT</a>
            <a href="#Assignments">Assignments</a>
        </div>
        <div class="Content">
            <div id="Experiment">
                Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. 
                At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, 
                consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. 
                At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.
            </div>
            <div id="HIT">
                Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. 
                At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, 
                consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. 
                At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.
            </div>
            <div id="Assignments">
                Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. 
                At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, 
                consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. 
                At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.
            </div>

        </div>
    </div>
</div>
</template>

<script lang="ts">
    import Vue from 'vue'
    import BaseHeadline from '@/components/BaseHeadline.vue'

    export default Vue.extend({
        name: 'Tags',
        components: {
            BaseHeadline,
        },
        data: () => ({
            route: {
                path: 'Overview',
                name: 'back to Overview',
            },
        })
    })
</script>

<style lang="scss">
    .Guide {
        position: relative
    }
    .GuideWrapper {
        display: flex;
        flex-direction: row;
        margin-top: 15%;
    }
    .SideBar {
        width: 10%;
        border-right: 1px solid grey; 
        padding: 20px;
        display: flex;
        flex-direction: column;
        position: fixed;
        height: 100%;
        a {
            margin-top: 20px;
        }
    }
    .Content{
        overflow: scroll;
        padding: 150px;
        margin-left: 10%;
        padding-top: 40px;
        text-align: justify;

        div {
            margin-top: 50px;
            font-size: 50px;
        }
    }
</style>