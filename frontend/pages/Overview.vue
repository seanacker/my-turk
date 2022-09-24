<template>
  <div class="Overview">
    <div class="HeaderWrapper">
      <BaseHeadline
        :route="route"
        prime
        title="myTurk"
        description="Overview of your experiments"
        :style="{ width: '80%' }"
      />
    </div>
    <div class="navigationWrapper">
      <BaseButton prime @click="addExperiment">new experiment</BaseButton>
      <BaseButton prime @click="addExperiment">guide</BaseButton>
      <BaseButton prime @click="refreshPage">refresh</BaseButton>
    </div>
    <BaseWrapper title="Production" red :style="{overflow: 'inherit'}" :hidden="prodIsHidden">
      <Table
        :experiments="experiments.production"
        @createHIT="createHIT"
        @onHitDeleteClick="handleDeleteHIT"
        @expireHIT="expireHIT"
        @deleteHIT="deleteHIT"
        @cancelHIT="cancelHIT"
      />
    </BaseWrapper>

    <BaseWrapper title="Sandbox" green :style="{overflow: 'inherit'}" :hidden="sandIsHidden">
      <Table
        :experiments="experiments.sandbox"
        @acceptAssignments="showAcceptAssignments"
        @rejectAssignment="showRejectAssignments"
        @createHIT="createHIT"
        @onHitDeleteClick="handleDeleteHIT"
        @expireHIT="expireHIT"
        @deleteHIT="deleteHIT"
        @cancelHIT="cancelHIT"
        @showAcceptAssignments="showAcceptAssignments"
        @showRejectAssignments="showRejectAssignments"
      />
    </BaseWrapper>

    <BaseModal
      :visible="modalIsVisible"
      title="Delete HIT"
      :cancel="{ label: 'cancel' }"
      :accept="{ label: 'accept', type: 'cancel' }"
      @onAccept="deleteHIT"
      @onCancel="closeModal"
    >      <p>Are you sure you want to delete this HIT?</p>
    </BaseModal>
    <BaseModal
      :visible="acceptAssignmentModalVisible"
      title="Accept Assignments"
      :rewardPerAssignment="rewardPerAssignment"
      :awardQualificationID="awardQualificationID"
      :cancel="{ label: 'cancel' }"
      :accept="{ label: 'Approve', type: 'success' }"
      type="accept"
      @onAccept="acceptAssignments"
      @onCancel="closeModal"
    >       
      <p>
        Before you approve Assignments make sure that, if necessary,
        you have qualified all Workers with their Award Qualification.
      </p>
      <p>
        Fill in a list of AssignmentIDs you want to accept.
        The List should be seperated by any non-alphabetic character.
      </p>
      <BaseTextarea 
        name="assignmentids"
        label="AssignmentIDs"
        @keyPress="setAcceptIDs"
      ></BaseTextarea>
      <BaseTextarea
        name="feedback"
        :style="{marginBottom: '15px', marginTop: '50px'}"
        label="Leave your own Feedback"
        :value="approvalFeedback"
        :onSave="true"
        @keyPress="setApprovalFeedbackFromText"
        @toggleSaveMessage="toggleSaveMessage()"
      />
      <p :style="{margin: '0 0  10px 0'}">or select a message</p>
      <select 
        :style="{margin: '0 0 15px 0', width: '100%'}" 
        class="MessageSelect"
        @change="setApprovalFeedbackFromSelect($event)"
      >
        <option 
          value="" 
          disabled 
          selected 
          v-text="approveMessages ? 'Select your message!' : 'No messages saved!'">
        </option>
        <option v-for="(option, i) in approveMessages" :key="i" :value="option.message" :style="{width: '100%', wordBreak: 'break-all'}">
          {{ option.message }}
        </option>
        
      </select>
      <p>
        Approving these Assignments will cost you {{this.priceForAccepting}}$
      </p>
    </BaseModal>
    <BaseModal
      :visible="rejectAssignmentModalVisible"
      title="Reject Assignments"
      :rewardPerAssignment="rewardPerAssignment"
      :cancel="{ label: 'cancel' }"
      :accept="{ label: 'Reject', type: 'warning' }"
      @onAccept="rejectAssignments"
      @onCancel="closeModal"
    > 
      <p>
        Before you reject Assignments make sure that, if necessary,
        you have qualified all Workers with their Award Qualification.
      </p>
      <p>
        Fill in a list of AssignmentIDs you want to reject.
        The List should be seperated by any non-alphabetic character.
      </p>
      <BaseTextarea 
        label="AssignmentIDs"
        @keyPress="setRejectIDs"
        name="assignmentids"
      ></BaseTextarea>
      <BaseTextarea
        name="feedback"
        :style="{marginBottom: '15px', marginTop: '50px'}"
        label="Leave your own Feedback"
        :value="rejectFeedback"
        :onSave="true"
        @keyPress="setRectionFeedbackFromText"
        @toggleSaveMessage="toggleSaveMessage()"
      />
      <p :style="{margin: '0 0  10px 0'}">or select a message</p>
      <div :style="{width: '100%'}">
        <select @change="setRejectionFeedbackFromSelect($event)" :style="{margin: '0 0 15px 0', display: 'inline-block%'}" class="MessageSelect" :disabled="!rejectMessages">
          <option 
            value="" 
            disabled 
            selected 
            v-text="approveMessages ? 'Select your message!' : 'No messages saved!'">
          </option>
          <option v-for="(option, i) in rejectMessages" :key="i" :value="option.message" :style="{width: '100%'}">
            {{ option.message }} 
          </option>
        </select>
      </div>
  </BaseModal>
  </div>
</template>
<script lang="ts">
import Vue from 'vue'

import BaseButton from '@/components/BaseButton.vue'
import BaseHeadline from '@/components/BaseHeadline.vue'
import BaseModal from '@/components/BaseModal.vue'
import BaseWrapper from '@/components/BaseWrapper.vue'
import Table from '@/components/overview/Table.vue'
import api from '@/api/index'
import { Experiment, Hit, Route, APIRes } from '@/lib/types'
import BaseTextarea from '~/components/BaseTextarea.vue'

type OverviewData = {
  route: Route
  hit: Hit | undefined
  modalIsVisible: boolean
  experiments: { production: Experiment[]; sandbox: Experiment[] }
  prodIsHidden: boolean
  sandIsHidden: boolean
  acceptAssignmentModalVisible: boolean
  rejectAssignmentModalVisible: boolean
  showHeaderMenu: boolean
  acceptIDs: string[]
  rejectIDs: string[]
  rewardPerAssignmentForModal?: string
  priceForAccepting?: number
  awardQualificationID: string | undefined
  approvalFeedback: string
  rejectFeedback: string
  saveMessage: boolean
  approveMessages: string[],
  rejectMessages: string[],
}

export default Vue.extend({
  name: 'Tags',
  components: {
    BaseHeadline,
    BaseModal,
    BaseWrapper,
    BaseButton,
    Table,
    BaseTextarea
},
  data: (): OverviewData => ({
    route: {
      path: 'index',
      name: 'Click here to SignOut',
      params: { loggedOut: true },
    },
    modalIsVisible: false,
    experiments: { production: [], sandbox: [] },
    prodIsHidden: false,
    sandIsHidden: false,
    hit: undefined,
    acceptAssignmentModalVisible: false,
    showHeaderMenu: false,
    rejectAssignmentModalVisible: false,
    acceptIDs: [],
    rejectIDs: [],
    rewardPerAssignmentForModal: undefined,
    priceForAccepting: undefined,
    awardQualificationID: undefined,
    approvalFeedback: "",
    rejectFeedback: "",
    saveMessage: false,
    approveMessages: [],
    rejectMessages: []
  }),
  mounted() {
    this.getExperiments()
    this.getMessages()
  },

  methods: {
    async getExperiments(): Promise<void> {
      const result = await api.getExperiments({ groupBy: 'endpoint' })
      this.prodIsHidden = result.endpoint === 'sandbox'
      console.log(this.prodIsHidden)
      this.sandIsHidden = result.endpoint === 'production'
      console.log(this.sandIsHidden)
      if (result.success) {
        console.log('data: ', result.data)
        this.experiments = result.data
        this.experiments.production = result.data.production || []
        this.experiments.sandbox = result.data.sandbox || []
        console.log(
          'sandbox experiments: ',
          JSON.stringify(result.data.sandbox)
        )
      }
    },
    async addExperiment(): Promise<void> {
      const res = await api.addExperiment({})

      if (res.success) {
        this.$router.push({
          name: 'Settings',
          params: { addExperiment: 'true' },
          query: { id: res.data.id },
        })
      } else {
        this.$toasted.show(res.message, {
          type: 'error',
          position: 'bottom-right',
          duration: 5000,
        })
      }
    },
    refreshPage() {
      this.experiments.production = []
      this.experiments.sandbox = []
      this.getExperiments()
    },
    async createHIT(experiment: Experiment, scheduledDateTime: string) {
      if (scheduledDateTime.includes('now')) scheduledDateTime = '0'
      const res = await api.createHIT({experiment, scheduledDateTime})
      console.log("createhit:res:",res)
      if (res.success) {
        this.$toasted.success(res.message, {
          position: 'bottom-right',
          duration: 3000,
        })
        const hit = res.data.HIT
        console.log("createhit:hit:", hit)
        experiment = this.addHITtoExperiment(experiment, hit)

        const id = experiment._id
        await api.saveSettings({ id, experiment })
        this.getExperiments()
      } else {
        this.$toasted.error(res.message, {
          position: 'bottom-right',
          duration: 5000,
        })
      }
    },
    async cancelHIT(hit: Hit){
      const deleteHITFromExperimentRes = await api.deleteHITFromExperiment(hit)
      this.$toasted.success(deleteHITFromExperimentRes.message, {
          position: 'bottom-right',
          duration: 3000,
        })
      console.log("cancel scheduled hit method called")
      await api.cancelScheduledHIT(hit)
      this.refreshPage()
    },
    async expireHIT(experiment: Experiment, hit: Hit) {
      const expireRes = await api.expireHIT({HITId: hit.HITId})
      this.$toasted.success(expireRes.message, {
          position: 'bottom-right',
          duration: 3000,
        })
      this.refreshPage()
    },
    async deleteHIT(experiment: Experiment, hit: Hit) {
      console.log("deleted HIT", hit)
      const deleteRes = await api.deleteHIT({HITId: hit.HITId})
        if (deleteRes.success) {
          this.$toasted.success(deleteRes.message, {
            position: 'bottom-right',
            duration: 3000,
          })
          experiment = this.deleteHITfromExperiment(experiment, hit)
          const id = experiment._id
          await api.saveSettings({ id, experiment })
          this.getExperiments()
        }
        else {
          this.$toasted.error(deleteRes.message, {
            position: 'bottom-right',
            duration: 5000,
          })
        }
        this.refreshPage()
    },
    deleteHITfromExperiment(experiment: Experiment, hit: Hit): Experiment {
      experiment.hits = experiment.hits.filter((_hit) => {
        return _hit.HITId != hit.HITId
      })
      return experiment
    },
    addHITtoExperiment(experiment: Experiment, hit: Hit) {
      const maxAssignments = hit.maxAssignments
      const available = hit.available
      const pending = hit.pending
      const completed = hit.completed
      const waitingForApproval = 
        `${parseInt(maxAssignments) - parseInt(available) - parseInt(completed) - parseInt(pending)}`

      const mHIT = {
        ...hit,
        available: `${available} / ${maxAssignments} `,
        pending: `${pending} / ${maxAssignments} `,
        waitingForApproval: `${waitingForApproval} / ${maxAssignments} `,
        completed: `${completed} / ${maxAssignments} `,
        maxAssignments,
      }

      if (!experiment.hits) {
        experiment.hits = []
      }
      experiment.hits.push(mHIT)
      return experiment
    },

    toggleHeaderMenu() {
      this.showHeaderMenu = !this.showHeaderMenu
    },

    showAcceptAssignments(rewardPerAssignment: string, awardQualificationID: string) {
      this.rewardPerAssignmentForModal = rewardPerAssignment
      this.awardQualificationID = awardQualificationID
      this.acceptAssignmentModalVisible = true
    },
    showRejectAssignments() {
      this.rejectAssignmentModalVisible = true
    },

    closeModal() {
      this.modalIsVisible = false
      this.acceptAssignmentModalVisible = false
      this.rejectAssignmentModalVisible = false
    },
    setAcceptIDs(value: any) {
      this.acceptIDs = value.assignmentids.split(/[^A-Za-z0-9]/).filter((value: string) => {
        return value != ""
      })
      this.priceForAccepting = this.rewardPerAssignmentForModal ? (parseInt(this.rewardPerAssignmentForModal)) * this.acceptIDs.length : undefined
      console.log("price in function: ", this.priceForAccepting)
    },
    setRejectIDs(value: any) {
      this.rejectIDs = value.assignmentids.split(/[^A-Za-z0-9]/).filter((value: string) => {
        return value != ""
      })
      console.log(this.rejectIDs)
    },
    async acceptAssignments() {
      if (this.saveMessage) {
          const messageRes = await api.createMessage(
            {message: this.approvalFeedback, type: 'approve'}
          )
          if (messageRes.success) {
            this.$toasted.show(messageRes.message, {
              type: 'success',
              position: 'bottom-right',
              duration: 3000,
            })
          } else {
            return this.$toasted.show(messageRes.message, {
              type: 'error',
              position: 'bottom-right',
              duration: 3000,
            })
          }
        }
      const res = await api.approveAssignments({assignmentIds: this.acceptIDs, awardQualificationID: this.awardQualificationID, feedback: this.approvalFeedback})
      console.log("res:", res)
      this.acceptAssignmentModalVisible = false
      if (res.success) {
        this.$toasted.success(res.message, {
            position: 'bottom-right',
            duration: 100000,
          })
      }
      else {
        res.data.filter((assignment: APIRes) => assignment.success).
          map((assignment: APIRes) => {
            this.$toasted.success(assignment.message, {
            position: 'bottom-right',
            duration: 5000,
          })
        })
        res.data.filter((assignment: APIRes) => !assignment.success).
          map((assignment: APIRes) => {
            this.$toasted.error(assignment.message, {
            position: 'bottom-right',
            duration: 5000,
          })
        })
      }
    },
    async rejectAssignments()  {
      if (this.saveMessage) {
        const messageRes = await api.createMessage({message: this.rejectFeedback, type: 'reject'})
        if (messageRes.success) {
          this.$toasted.show(messageRes.message, {
            type: 'success',
            position: 'bottom-right',
            duration: 3000,
          })
        } else {
          return this.$toasted.show(messageRes.message, {
            type: 'error',
            position: 'bottom-right',
            duration: 3000,
          })
        }
      }
      const res = await api.rejectAssignments({assignmentIds: this.rejectIDs, feedback: this.rejectFeedback})
      console.log("res:",)
      this.acceptAssignmentModalVisible = false
      if (res.success) {
        this.$toasted.success(res.message, {
            position: 'bottom-right',
            duration: 5000,
          })
      }
      else {
        res.data.filter((assignment: APIRes) => assignment.success).
          map((assignment: APIRes) => {
            this.$toasted.success(assignment.message, {
            position: 'bottom-right',
            duration: 5000,
          })
        })
        res.data.filter((assignment: APIRes) => !assignment.success).
          map((assignment: APIRes) => {
            this.$toasted.error(assignment.message, {
            position: 'bottom-right',
            duration: 5000,
          })
        })
      }
    },
    async getMessages() {
      const approveRes = await api.getMessages({type: 'approve'})
      this.approveMessages = approveRes.data
      const rejectRes = await api.getMessages({type: 'reject'})
      this.rejectMessages = rejectRes.data
    },
    setApprovalFeedbackFromText(val: any): void {
      this.approvalFeedback = val.feedback
    },
    setApprovalFeedbackFromSelect(event: any): void {
      this.approvalFeedback = event.target.value
    },
    setRectionFeedbackFromText(val: any): void {
      this.rejectFeedback = val.feedback
    },
    setRejectionFeedbackFromSelect(event: any): void {
      this.rejectFeedback = event.target.value
    },
    toggleSaveMessage(){ 
      this.saveMessage = !this.saveMessage
    },

  },
})
</script>
<style lang="scss">
.Overview {
  position: relative;
}

.navigationWrapper{
  position: absolute;
  top: 0;
  right: 0;
  display: flex;
  flex-direction: row;

  > .BaseButton {
    margin-right: 10px;
  }
}
.HeaderWrapper {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
}

.HeaderMenu {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border: 1px solid black;
  box-shadow: 0px 8px 15px rgba(0, 0, 0, 0.2);
  margin-right: 4px;
}

.headerMenuButton {
  margin-left: auto;
  border: 0;
  background: transparent;
}
</style>
