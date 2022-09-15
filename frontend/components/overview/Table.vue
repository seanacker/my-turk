<template>
  <div >
    <table :style="{minHeight: modalIsVisible ? '500px' : '', transition: 'all 0.5s ease-out'}">
      <tr v-if="!experiments" light>
        <td class="is-loading">Title</td>
        <td class="is-loading">Description</td>
        <td class="is-loading align-right">Available</td>
        <td class="is-loading align-right">Pending</td>
        <td class="is-loading align-right">Waiting</td>
        <td class="is-loading align-right">Completed</td>
        <td class="is-loading">blub</td>
      </tr>

      <tr v-else light>
        <td>Title</td>
        <td>Description</td>
        <td class="align-right">Available</td>
        <td class="align-right">Pending</td>
        <td class="align-right">Waiting<br/> for<br/> approval</td>
        <td class="align-right">Completed</td>
        <td></td>
      </tr>
      <template v-for="experiment in experiments" bold>
        <tr :key="experiment._id">
          <td >
            <div class="Anchor" @click="onExperimentClick(experiment)" :style="{display: 'flex'}">
              <div class="no-wrap">{{experiment.experimentName}}</div>          
              <fa v-if="activeExperimentId==experiment._id" icon="arrow-up"/>
              <fa v-else icon="arrow-down"/>
            </div>
            <span v-if="activeExperimentId==experiment._id" :style="{display: 'flex', flexDirection: 'column', border: '1px solid black', borderRadius: '5px', marginTop: '10px'}">
              <span @click="onExperimentEditClick(experiment)" :style="{textAlign: 'center', paddingTop: '5px', paddingBottom: '5px'}" class="hoverable">edit</span>
              <span @click="onExperimentOverviewClick(experiment)" :style="{textAlign: 'center', paddingBottom: '5px', paddingTop: '5px'}" class="hoverable">overview</span>
            </span>
          </td>
          <td>{{ experiment.description }}</td>
          <td class="align-right">{{ experiment.available }}</td>
          <td class="align-right">{{ experiment.pending }}</td>
          <td class="align-right">{{ experiment.waitingForApproval}}</td>
          <td class="align-right">{{ experiment.completed }}</td>
          <td class="button align-center" >
            <BaseButton
                v-if="experiment.endpoint !== 'development'"
                second
                square
                notLast
                title="new hit"
                fullWidth
                @click="onNewHitClick(experiment)"
              />
            </td>
            <td class="button">
              <BaseButton
              v-if="experiment.endpoint !== 'development'"
              second
              square
              notLast
              title="qualify all"
              fullWidth
              @click="onNewHitClick(experiment)"
            />
          </td>
          <td class="button">
            <BaseButton
              v-if="experiment.endpoint !== 'development'"
              second
              square
              notLast
              title="qualify all"
              fullWidth
              @click="onQualifyAllClick(experiment)"
            />
          </td>
          <td class="button">
              <BaseButton
                v-if="experiment.endpoint !== 'development'"
                second
                square
                notLast
                title="notify workers"
                fullWidth
                @click="modalIsVisible=true"
              />
            </td>
          <td class="button">
            <BaseButton
              v-if="experiment.endpoint !== 'development'"
              second
              square
              notLast
              title="approve workers"
              @click="onApproveWorkersClick(experiment.rewardPerAssignment, experiment.awardQualificationID)"
            />
          </td>
          <td class="button">
            <BaseButton
              v-if="experiment.endpoint !== 'development'"
              second
              square
              title="reject workers"
              @click="onRejectWorkersClick()"
            />
          </td>
      </tr>
      <template v-for="(hit, index) in experiment.hits" >
        <tr :key="hit.HITId + 'header'">
          <td>
            <input :id="hit.HITId" class="toggle" type="checkbox" @click="toggleActiveHIT(hit.HITId)"/>
            <label :for="hit.HITId" class="lbl-toggle">Details</label>
          </td>
          <td :style="{whiteSpace: 'nowrap'}">
            {{ index + 1 }}: <span :style="{fonts}">{{ hit.HITId }}</span>&nbsp;
            <BaseCopy :value="hit.HITId" />
          </td>
          <td class="align-right">{{ hit.available }}</td>
          <td class="align-right">{{ hit.pending }}</td>
          <td class="align-right">{{ hit.waitingForApproval }}</td>
          <td class="align-right">{{ hit.completed }}</td>
          <td class="button">
              <BaseButton notLast second square fullWidth @click="onHitClick(hit, experiment)">
                Fullscreen
              </BaseButton>
          </td>
          <td v-if="hitStatus(hit)=='cancelable'" class="button">
            <BaseButton second square fullWidth @click="onCancelHitClick(experiment, hit)">
              Expire
            </BaseButton>
          </td>
          <td v-if="hitStatus(hit)=='expireable'" class="button">
            <BaseButton  second square fullWidth @click="onExpireHitClick(experiment, hit)">
              Expire
            </BaseButton>
          </td>
          <td v-if="hitStatus(hit)=='deleteable'" class="button">
            <BaseButton  second square fullWidth @click="onDeleteHitClick(experiment, hit)">
              Delete
            </BaseButton>
          </td>
          <td v-if="hitStatus(hit)=='approvable'" class="button">
            <BaseButton  second square fullWidth @click="onQualifyAllFromHitClick(experiment, hit)">
              Qualify all
            </BaseButton>
          </td>
        </tr>
        <tr v-if="activeHITId==hit.HITId" class="collapsible-content" :key="hit.HITId + 'workers'">
          <td colspan="100%" :style="{paddingLeft: '50px'}">
            <WorkersInline
              :HITId="hit.HITId"
              :awardid="experiment.awardQualificationId"
            />
          </td>
        </tr>
        <BaseModal
          :visible="modalIsVisible"
          :key=""
          title="Send Email"
          :cancel="{ label: 'cancel' }"
          :accept="{ label: 'send', type: 'success' }"
          :style="{width:'100%', margin: 'auto'}"
          @onAccept="notifyWorkers(experiment)"
          @onCancel="closeModal"
        >
          <BaseTextarea
            name="subject"
            :style="{marginBottom: '15px', marginTop: '50px'}"
            label="Please enter the subject of your email"
            :value="emailSubject"
            @keyPress="setEmailSubject"
          />
          <BaseTextarea
            name="message"
            :style="{marginBottom: '15px', marginTop: '50px'}"
            label="Please enter your message"
            :value="emailMessage"
            @keyPress="setEmailMessage"
          />
        </BaseModal>
      </template>
      </template>
    </table>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

import BaseButton from '../BaseButton.vue'
import BaseCopy from '../BaseCopy.vue'
import BaseModal from '../BaseModal.vue'
import WorkersInline from '../../pages/WorkersInline.vue'
import { Experiment, Hit, Assignment } from '../../lib/types'
import api from '@/api'

export default Vue.extend({
  name: 'Tags',
  components: {
    BaseButton,
    BaseCopy,
    BaseModal,
    WorkersInline,
  },
  props: {
    experiments: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    activeExperimentId: '',
    activeHITId: '',
    emailSubject: '',
    emailMessage: '',
    modalIsVisible: false

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
      this.$emit('deleteHIT', experiment, hit)
    },
    onNewHitClick(experiment: Experiment) {
      this.$emit('createHIT', experiment)
    },
    onApproveWorkersClick(rewardPerAssignment: string, awardQualificationID: string) {
      this.$emit('showAcceptAssignments', rewardPerAssignment, awardQualificationID)
    },
    onRejectWorkersClick() {
      this.$emit('showRejectAssignments')
    },
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
    toggleActiveHIT(HITId: string) {
      if(this.activeHITId == HITId) this.activeHITId = ""
      else this.activeHITId = HITId
    },
    closeModal() {
        this.modalIsVisible = false
    },
    setEmailSubject(val: any): void {
      this.emailSubject = val.subject
    },
    setEmailMessage(val: any): void {
      this.emailMessage = val.message
    },
    async notifyWorkers(experiment: Experiment) {
      const activeExperiment = (this.experiments as Experiment[]).filter((iterExperiment: Experiment) => iterExperiment._id == experiment._id
      )[0]
      const hitList = activeExperiment.hits
      for (const hit of hitList) {
        const assignmentRes = await api.listAssignments({ HITId: hit.HITId })
        if (assignmentRes.success) {
            const workerIDs = assignmentRes.data.map((assignment: Assignment) => assignment.WorkerId)
            const notificationRes = await api.notifyWorkers({subject: 'noob', message: 'hallo', workerIDs})
            if(notificationRes.success) {
              this.$toasted.success(notificationRes.message, {
                position: 'bottom-right',
                duration: 3000,
              })
            }
            else {
              this.$toasted.error(notificationRes.message, {
                position: 'bottom-right',
                duration: 3000,
              })
            }
          }     
        this.closeModal()
      }
    },
  }
})
</script>
<style lang="scss">
  table {
    width: 100%;
    border-spacing: 0 1em;
  }
  tr {
    width: 100%;
    margin-top: 10px;
  }
  td {
    padding: 0 10px;
    font-size: 12px;
  }
  td.button {
    padding: 0
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
  overflow: scroll;
  transition: max-height 0.25s ease-in-out;
  max-height: 100vh;
}

.hoverable:hover{
  background-color: rgba(color(gray-dark), 0.25);
}

.buttons {
  width: 40%;
  display: flex;
}
.no-wrap {
  white-space: nowrap
}
</style>
