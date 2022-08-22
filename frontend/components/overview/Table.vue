<template>
  <div class="Table">
    <BaseRow width="70%" v-if="!experiments" light>
      <span class="is-loading">Title</span>
      <span class="is-loading is-wide">Description</span>
      <span class="is-loading is-narrow align-right">Available</span>
      <span class="is-loading is-narrow align-right">Pending</span>
      <span class="is-loading is-narrow align-right">Waiting</span>
      <span class="is-loading is-narrow align-right">Completed</span>
      <span class="buttons">&nbsp;</span>
    </BaseRow>

    <BaseRow v-else :width="'60%'" light>
      <span class="is-narrow">Title</span>
      <span class="is-wide">Description</span>
      <span class="is-narrow align-right">Available</span>
      <span class="is-narrow align-right">Pending</span>
      <span class="is-narrow align-right">Waiting for approval</span>
      <span class="is-narrow align-right">Completed</span>
      <span class="buttons">&nbsp;</span>
      
    </BaseRow>

    <BaseRow v-for="experiment in experiments" :key="experiment._id" bold>
      <span class="Anchor is-narrow" @click="onExperimentClick(experiment)">
        {{ experiment.experimentName }}&nbsp;
        <i class="far fa-edit"></i>
      </span>
      <span class="is-wide">{{ experiment.description }}</span>
      <span class="is-narrow align-center">{{ experiment.available }}</span>
      <span class="is-narrow align-center">{{ experiment.pending }}</span>
      <span class="is-narrow align-center">{{
        experiment.waitingForApproval
      }}</span>
      <span class="is-narrow align-center">{{ experiment.completed }}</span>
      <span class="is-narrow align-center">
        <BaseButton
          v-if="experiment.endpoint !== 'development'"
          second
          square
          title="new hit"
          @click="onNewHitClick(experiment)"
        />
      </span>
      <span class="is-narrow align-center">
        <BaseButton
          v-if="experiment.endpoint !== 'development'"
          second
          square
          title="qualify all"
          @click="onQualifyAllClick(experiment)"
        />
      </span>
      <span class="is-narrow">
        <BaseButton
          v-if="experiment.endpoint !== 'development'"
          second
          square
          title="approve workers"
          @click="onApproveWorkersClick(experiment.rewardPerAssignment)"
        />
      </span>
      <span class="is-narrow">
        <BaseButton
          v-if="experiment.endpoint !== 'development'"
          second
          square
          title="reject workers"
          @click="onQualifyAllClick(experiment)"
        />
      </span>
      <BaseRow v-for="(hit, index) in experiment.hits" :key="hit.HITId">
        <span :style="{ width : '28%', paddingLeft: '10px'}">
          <input :id="hit.HITId" class="toggle" type="checkbox" />
          <label :for="hit.HITId" :style="{textAlign: 'left', paddingLeft: '0'}" class="lbl-toggle">Details</label>

          <span :style="{ fontSize: '0.7rem'}">
            {{ index + 1 }}: {{ hit.HITId }}&nbsp;
            <BaseCopy :value="hit.HITId" />
          </span>
        </span>
        <span class="is-narrow align-center">{{ hit.available }}</span>
        <span class="is-narrow align-center">{{ hit.pending }}</span>
        <span class="is-narrow align-center">{{ hit.waitingForApproval }}</span>
        <span class="is-narrow align-center">{{ hit.completed }}</span>
        <span class="is-narrow align-center Anchor" @click="onHitClick(hit, experiment)"
          >Fullscreen</span
        >
        <span class="is-narrow align-center Anchor" @click="onExpireAndDeleteClick(experiment, hit)"
          >Expire</span
        >
        <span :style="{width: '25%'}">&nbsp;</span>
        <WorkersInline
          :HITId="hit.HITId"
          :awardid="experiment.awardQualificationId"
        />
      </BaseRow>
    </BaseRow>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

import BaseButton from '../BaseButton.vue'
import BaseCopy from '../BaseCopy.vue'
import BaseRow from '../BaseRow.vue'
import WorkersInline from '../../pages/WorkersInline.vue'
import { Experiment, Hit } from '../../lib/types'

export default Vue.extend({
  name: 'Tags',
  components: {
    BaseButton,
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
  data: () => ({}),
  methods: {
    onExperimentClick(experiment: Experiment) {
      this.$router.push({
        name: 'Settings',
        query: { id: experiment._id },
        params: { experiment: experiment as any, initial: 'false' },
      })
    },
    onHitClick(hit: Hit, experiment: Experiment) {
      this.$router.push({
        name: 'Workers',
        params: {},
        query: {      
          HITId: hit.HITId,
          awardQualificationID: experiment.awardQualificationId
        },
      })
    },
    onExpireAndDeleteClick(experiment: Experiment, hit: Hit) {
      this.$emit('expireAndDeleteHIT', experiment, hit)
    },
    onNewHitClick(experiment: Experiment) {
      this.$emit('createHIT', experiment)
    },
    onApproveWorkersClick(rewardPerAssignment: string) {
      console.log("here", rewardPerAssignment)
      this.$emit('showAcceptAssignments', rewardPerAssignment)
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
  },
})
</script>
<style lang="scss">
.Table {
  display: flex;
  flex-wrap: wrap;
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

.buttons {
  width: 40%;
  display: flex;
}
</style>
