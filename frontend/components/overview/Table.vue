<template>
  <div class="Grid">
    <BaseRow v-if="!experiments" light>
      <span class="is-loading is-narrow">Title</span>
      <span class="is-loading is-wide">Description</span>
      <span class="is-loading is-narrow align-right">Available</span>
      <span class="is-loading is-narrow align-right">Pending</span>
      <span class="is-loading is-narrow align-right">Waiting</span>
      <span class="is-loading is-narrow align-right">Completed</span>
      <span class="is-loading is-wide"></span>
    </BaseRow>

    <BaseRow v-else light>
      <span class="is-narrow">Title</span>
      <span class="is-wide">Description</span>
      <span class="is-narrow align-right">Available</span>
      <span class="is-narrow align-right">Pending</span>
      <span class="is-narrow align-right">Waiting for approval</span>
      <span class="is-narrow align-right">Completed</span>
      <span class="is-wide"></span>
    </BaseRow>
    <div v-for="experiment in experiments" :key="experiment._id">
    <BaseRow  bold>
      <span class="is-narrow" :style="{display: 'flex', flexDirection: 'column'}">
        <span class="Anchor" @click="onExperimentClick(experiment)">
          <fa v-if="activeExperimentId==experiment._id" icon="arrow-up"/>
          <fa v-else icon="arrow-down"/>
          {{experiment.experimentName}}          
        </span>
        <span v-if="activeExperimentId==experiment._id" :style="{display: 'flex', flexDirection: 'column', border: '1px solid black', borderRadius: '5px'}">
          <span @click="onExperimentEditClick(experiment)" :style="{paddingLeft: '10px'}">edit</span>
          <span @click="onExperimentOverviewClick(experiment)" :style="{paddingLeft: '10px'}">overview</span>
        </span>

      </span>
      <span class="is-wide">{{ experiment.description }}</span>
      <span class="is-narrow align-right">{{ experiment.available }}</span>
      <span class="is-narrow align-right">{{ experiment.pending }}</span>
      <span class="is-narrow align-right">{{
        experiment.waitingForApproval
      }}</span>
      <span class="is-narrow align-right">{{ experiment.completed }}</span>
      <span class="is-wide align-center" :style="{display: 'flex'}">
        <BaseButtons
          v-if="experiment.endpoint !== 'development'"
          second
          square
          title="new hit"
          @click="onNewHitClick(experiment)"
        /><br />
        <BaseButtons
          v-if="experiment.endpoint !== 'development'"
          second
          square
          title="qualify all"
          @click="onQualifyAllClick(experiment)"
        />
      </span>
      </BaseRow>
      <BaseRow v-for="(hit, index) in experiment.hits" :key="hit.HITId">
        <span class="is-narrow">
          <input :id="hit.HITId" class="toggle" type="checkbox" />
          <label :for="hit.HITId" class="lbl-toggle">Details</label>
        </span>
        <span class="is-wide">
          {{ index + 1 }}: <span :style="{fonts}">{{ hit.HITId }}</span>&nbsp;
          <BaseCopy :value="hit.HITId" />
        </span>
        <span class="is-narrow align-right">{{ hit.available }}</span>
        <span class="is-narrow align-right">{{ hit.pending }}</span>
        <span class="is-narrow align-right">{{ hit.waitingForApproval }}</span>
        <span class="is-narrow align-right">{{ hit.completed }}</span>
        <div class="is-wide align-center" :style="{dislay: 'flex', flexDirection: 'row'}">
          <BaseButtons second square class="Anchor" @click="onHitClick(hit, experiment)">
            Fullscreen
          </BaseButtons>
          <BaseButtons v-if="hitStatus(hit)=='cancelable'" second square @click="onCancelHitClick(experiment, hit)">
            Expire
          </BaseButtons>
          <BaseButtons v-if="hitStatus(hit)=='expireable'" second square @click="onExpireHitClick(experiment, hit)">
            Expire
          </BaseButtons>
          <BaseButtons v-if="hitStatus(hit)=='deleteable'" second square @click="onDeleteHitClick(experiment, hit)">
            Delete
          </BaseButtons>
          <BaseButtons v-if="hitStatus(hit)=='approvable'" second square @click="onQualifyAllFromHitClick(experiment, hit)">
            Qualify all
          </BaseButtons>
        </div>
        <WorkersInline
          :HITId="hit.HITId"
          :awardid="experiment.awardQualificationId"
        />
      </BaseRow>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

import BaseButtons from '../BaseButton.vue'
import BaseCopy from '../BaseCopy.vue'
import BaseRow from '../BaseRow.vue'
import WorkersInline from '../../pages/WorkersInline.vue'
import { Experiment, Hit } from '../../lib/types'

export default Vue.extend({
  name: 'Tags',
  components: {
    BaseButtons,
    BaseCopy,
    BaseRow,
    WorkersInline,
  },
  props: {
    experiments: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    activeExperimentId: ''
  }),
  methods: {
    onExperimentClick(experiment: Experiment) {
      if(this.activeExperimentId == experiment._id) this.activeExperimentId = ""
      else this.activeExperimentId = experiment._id
    },
    onExperimentEditClick(experiment: Experiment) {
      this.$router.push({
        name: 'Settings',
        query: { id: experiment._id },
        params: { experiment: experiment as any, initial: 'false' },
      })
    },
    onExperimentOverviewClick(experiment: Experiment) {
      const hitList = experiment.hits.map(hit => hit.HITId).toString()
      this.$router.push({
        name: 'Workers',
        query: {
          awardQualificationID: experiment.awardQualificationId,
          title: experiment.title,
          hitList,
          experimentId: experiment._id
        }
      })

    },
    onHitClick(hit: Hit, experiment: Experiment) {
      this.$router.push({
        name: 'Workers',
        params: {},
        query: {      
          HITId: hit.HITId,
          awardQualificationID: experiment.awardQualificationId,
        },
      })
    },
    onCancelHitClick(experiment: Experiment, hit: Hit) {
      this.$emit('cancelHIT', experiment, hit)
    },
    onExpireHitClick(experiment: Experiment, hit: Hit) {
      this.$emit('expireHIT', experiment, hit)
    },
    onDeleteHitClick(experiment: Experiment, hit: Hit) {
      console.log("emitting delete hit")
      this.$emit('deleteHIT', experiment, hit)
    },
    onNewHitClick(experiment: Experiment) {
      this.$emit('createHIT', experiment)
    },
    // todo: Implement Button that deletes hit
    // onHitDeleteClick(hit: Hit) {
    //   this.$emit('onHitDeleteClick', hit)
    // },
    onQualifyAllClick() {
      // TODO: actually keep track of who is qualified already
      // TODO: Approve Workers from datastructure instead of simulating button presses
      const btns = document.getElementsByClassName('QualifyBtn') as HTMLCollectionOf<HTMLButtonElement>
      for (let i = 0; i < btns.length; i++) {
        btns[i].click()
      }
    },
    hitStatus(hit: Hit) {
      const pending = parseInt(hit.pending.split('/')[0])
      const waitingForApproval = parseInt(hit.waitingForApproval.split('/')[0])
      console.log(hit)
      if (hit.status == "Pending") return 'cancelable'
      if (hit.status == "Assignable") return 'expireable'
      if (waitingForApproval == 0 && pending == 0) return 'deleteable'     
      if (hit.status == "Reviewable" || hit.status == "Reviewing") return 'approvable'    
      return 
    },
  },
})
</script>
<style lang="scss">
.Table {
  display: grid;
  flex-wrap: wrap;
  grid-template-columns: 1fr 3fr 1fr 1fr 1fr 1fr 3fr;
}
.Overview input[type='checkbox'] {
  display: none;
}
.lbl-toggle {
  display: block;

  font-weight: bold;
  font-family: monospace;
  text-transform: uppercase;
  text-align: center;

  padding: 8px;

  color: #a77b0e;

  cursor: pointer;

  transition: all 0.25s ease-out;

  flex: 1.5;
}

.lbl-toggle:hover {
  color: #7c5a0b;
}

.lbl-toggle::before {
  content: ' ';
  display: inline-block;

  border-top: 5px solid transparent;
  border-bottom: 5px solid transparent;
  border-left: 5px solid currentColor;

  vertical-align: middle;
  margin-right: 0.7rem;
  transform: translateY(-2px);

  transition: transform 0.2s ease-out;
}

.toggle:checked ~ .lbl-toggle::before {
  //border-right: 5px solid transparent;
  //border-top: 5px solid currentColor;
  //border-left: 5px solid transparent;
  transform: rotate(90deg);
}

.collapsible-content {
  max-height: 0px;
  overflow: hidden;

  transition: max-height 0.25s ease-in-out;
}

.toggle:checked ~ .collapsible-content {
  max-height: 100vh;
  overflow: scroll;
}
</style>
