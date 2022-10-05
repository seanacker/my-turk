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
            <a href="#MTurk">MTurk</a>
            <a href="#myTurk">myTurk</a>
            <a href="#Experiment">Experiments</a>
            <a href="#HIT">HIT</a>
            <a href="#Assignments">Assignments</a>
        </div>
        <div class="Content">
            <div id="MTurk">
                <h4>What is MTurk?</h4>
                <p>
                    Amazon Mechanical Turk (MTurk) is a crowdsourcing marketplace that connects you with an on-demand, scalable, human workforce to complete tasks(HITs). To use it you need a
                    Mechanical Turk account and an AWS account. You need to link those. The setup for this is best described on the <a href="https://docs.aws.amazon.com/AWSMechTurk/latest/AWSMechanicalTurkRequester/SetUpMturk.html">MTurk Guide</a>.
                </p>
            </div>
            <hr/>
            <div id="myTurk">
                <h4>What is myTurk</h4>
                <p>
                    "myTurk" is a experiment organization tool based on publishing tasks to MTurk. It improves the userexperience of MTurk in 2 major areas: <br/><br/>
                    <b>Hitmanagement:</b>
                    <ul>
                        <li>schedule Hits</li>
                        <li>group HITs to experiments</li>
                        <li>improved control on who participates on a HIT</li>
                    </ul>
                    <b>Workermanagment</b>
                    <ul>
                        <li>Improved review of assignments</li>
                        <li>Notify workers who participated in previous experiments</li>
                    </ul>
                </p>
            </div>
            <div id="Assignments">
                <h4>Experiments</h4>
                <p>
                    To start working with myTurk you need to create an experiment.  It is not a tool ment to develop the survey itself, there are numerous tools to support you in that area (e.g. <a href="https://www.limesurvey.org/de/">limesurvey</a>). 
                    To create the experiment enter the entrypoint of the 3rd party survey in the regarding field. <br/>
                    
                </p>
                <p>
                    You can also decide to generate a qualification ID for the experiment. This ID can then be assigned to every worker who participated on a HIT of the experiment. On the basis of those IDs workers can be included or excluded of the possible participation pool.
                    By checking the "Make the qualification ID from this experiment an exclusion criteria for this HIT" you garantee that once the generated qualification ID for this experiment is assigned to workers, they can not participate on any HIT of that experiment.
                    <image src="~/assets/requirementsAndEntrypoint.png"></image>

                </p>
            </div>
        <div id="HIT">
            <h4>HITs</h4>
            <p>
                MTurk charges more service fee per Worker if you publish HITs with more then 10 possible Assignments. Therefore it is highly recommended to publish your Experiment in batches. These batches are equivalent to MTurks HITs. A HIT gains all the attributes of the 
                containing experiment, including the entrypoint. You can 
            </p>
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
    p {
        font-size: 24px;
    }
</style>