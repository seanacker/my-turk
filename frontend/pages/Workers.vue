<template>
  <div class="Workers">
    <BaseHeadline
      :route="route"
      :title="title"
      :description="isExperimentView ? null : `Started: ${date}`"
      :meta="isExperimentView ? `ExperimentID: ${experimentId}` : `HIT: ${HITId}`"
    />
    <BaseWrapper :title="'Workers waiting for approval (' +  submitted?.length + ')'" gray-dark>
      <TableSubmitted
        :workers="submitted"
        :isExperimentView="isExperimentView"
        @onApprove="handleApprove"
        @onReject="handleReject"
        @onQualify="handleQualify"
      />
    </BaseWrapper>
    <BaseWrapper :title="'Workers approved (' + approved?.length +')' " green>
      <TableApproved :workers="approved" :isExperimentView="isExperimentView"/>
    </BaseWrapper>
    <BaseWrapper :title="'Workers rejected (' + rejected?.length + ')'" red>
      <TableRejected :workers="rejected" :isExperimentView="isExperimentView"/>
    </BaseWrapper>
    <BaseButton prime title="refresh" @click="refreshPage" />
    <BaseButton second title="Qualify All" @click="handleQualifyAll"/>

    <BaseModal
      :visible="modalApproveIsVisible"
      title="Approve Assignment"
      :cancel="{ label: 'cancel' }"
      :accept="{ label: 'approve', type: 'success' }"
      @onAccept="approveAssignment"
      @onCancel="closeModal"
    >
      <BaseTextarea
        name="feedback"
        :style="{marginBottom: '15px', marginTop: '50px'}"
        label="Please fill in the Message you want to leave for the worker"
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
          :style="{width: '100%'}" 
          v-text="approveMessages ? 'Select your message!' : 'No messages saved!'">
        </option>
        <option :style="{width: '100%'}" v-for="(option, i) in approveMessages" :key="i" :value="option.message">
          {{ option.message }}
        </option>
        
      </select>
    </BaseModal>

    <BaseModal
      :visible="modalRejectIsVisible"
      title="Reject Assignment"
      :cancel="{ label: 'cancel' }"
      :accept="{ label: 'reject', type: 'warning' }"
      @onAccept="rejectAssignment"
      @onCancel="closeModal"
    >    
      <BaseTextarea
        name="feedback"
        :style="{marginBottom: '15px', marginTop: '50px'}"
        label="Please fill in the Message you want to leave for the worker"
        :value="rejectFeedback"
        :onSave="true"
        @keyPress="setRectionFeedbackFromText"
        @toggleSaveMessage="toggleSaveMessage()"
      />
      <p :style="{margin: '0 0  10px 0'}">or select a message</p>
      <select @change="setRejectionFeedbackFromSelect($event)" :style="{margin: '0 0 15px 0', width: '100%'}" class="MessageSelect" :disabled="!rejectMessages">
        <option 
          value="" 
          disabled 
          selected 
          :style="{width: '100%'}"
          v-text="approveMessages ? 'Select your message!' : 'No messages saved!'">
        </option>
        <option v-for="(option, i) in rejectMessages" :key="i" :style="{width: '100%'}" :value="option.message">
          {{ option.message }} 
        </option>
      </select>
    </BaseModal>
  </div>
</template>
<script lang="ts">
import Vue from 'vue'
import Moment from 'moment'

import BaseButton from '@/components/BaseButton.vue'
import BaseHeadline from '@/components/BaseHeadline.vue'
import BaseModal from '@/components/BaseModal.vue'
import BaseTextarea from '@/components/BaseTextarea.vue'
import BaseWrapper from '@/components/BaseWrapper.vue'
import TableApproved from '@/components/workers/Table-Approved.vue'
import TableRejected from '@/components/workers/Table-Rejected.vue'
import TableSubmitted from '@/components/workers/Table-Submitted.vue'
import api from '@/api'
import { WorkersData, Worker } from '@/lib/types'
import BaseSelect from '@/components/BaseSelect.vue'

export default Vue.extend({
  name: 'Tags',
  components: {
    BaseButton,
    BaseModal,
    BaseHeadline,
    BaseTextarea,
    BaseWrapper,
    BaseSelect,
    TableApproved,
    TableRejected,
    TableSubmitted
},
  props: {},
  data: (): WorkersData => ({
    isExperimentView: undefined,
    experimentId: '',
    HITId: '',
    title: '',
    creationTime: '',
    awardQualificationID: '',
    modalRejectIsVisible: false,
    modalApproveIsVisible: false,
    rejectFeedback:
      'Worker did not submit any data, our server shows no HIT-relevant activity whatsoever for this assigment.',
    approvalFeedback: 'Thank you very much for participating!',
    assignmentID: '',
    workerID: '',
    route: {
      path: 'Overview',
      name: 'back to Overview',
    },
    submitted: [],
    approved: [],
    rejected: [],
    approveMessages: [],
    rejectMessages: [],
    saveMessage: false
  }),
  computed: {
    date: {
      get(): string {
        return Moment(this.creationTime).format('DD.MM.YYYY - HH:mm:ss')
      },
      set() {},
    },
  },
  mounted: async function (){
    this.awardQualificationID = this.$route.query.awardQualificationID as string || ''
    this.isExperimentView = this.$route.query.hasOwnProperty('hitList')
    await this.getWorkers()
    if (this.isExperimentView) {
      this.getExperiment()
    }
    else this.getHIT()
    await this.getMessages()
  },
  methods: {
    async getHIT(): Promise<void> {
      const HITId = this.$route.query.HITId || ''
      const res = await api.getHIT({ HITId })

      if (res.success) {
        const hit = res.data
        this.HITId = hit.HITId
        this.title = hit.Title
        this.creationTime = hit.creationTime
      } else {
        this.$toasted.show(res.message, {
          type: 'error',
          position: 'bottom-right',
          duration: 3000,
        })
      }
    },
    async getExperiment(): Promise<void> {
      this.title = this.$route.query.title as string
      this.experimentId = this.$route.query.experimentId as string
    },
    clearWorkers(): void {
      this.submitted = []
      this.approved = []
      this.rejected = []
    },
    async getWorkers(): Promise<void> {
      this.clearWorkers()
      var hitList = []
      if (this.isExperimentView){
        hitList = (this.$route.query.hitList as string).split(',') || []
      }
      else {
        hitList = [this.$route.query.HITId || '']
      }
      for (const HITId of hitList) {
        const res = await api.listAssignments({ HITId })      
        if (res.success) {
          const assignments = res.data
          for (const assignment of assignments) {
            const id = assignment.WorkerId
            const assignmentID = assignment.AssignmentId
            const startTime = Moment(assignment.AcceptTime).format('HH:mm:ss')
            const startDate = Moment(assignment.AcceptTime).format('DD.MM.YYYY')
            const finishTime = Moment(assignment.SubmitTime).format('HH:mm:ss')
            const finishDate = Moment(assignment.SubmitTime).format('DD.MM.YYYY')
            const status = assignment.AssignmentStatus.toLowerCase()

            const worker: Worker = {
              id,
              HITId: HITId as string,
              awardQualificationId: this.awardQualificationId,
              assignmentID,
              started: {
                time: startTime,
                date: startDate,
              },
              finished: {
                time: finishTime,
                date: finishDate,
              },
            }

            if (status === 'rejected') {
              const rejectionTime = Moment(assignment.RejectionTime).format(
                'HH:mm:ss'
              )
              const rejectionDate = Moment(assignment.RejectionTime).format(
                'DD.MM.YYYY'
              )
              worker.rejected = {
                time: rejectionTime,
                date: rejectionDate,
              }
              this.rejected?.push(worker)
            } else if (status === 'approved') {
              const approvedTime = Moment(assignment.ApprovalTime).format(
                'HH:mm:ss'
              )
              const approvedDate = Moment(assignment.ApprovalTime).format(
                'DD.MM.YYYY'
              )
              worker.approved = {
                time: approvedTime,
                date: approvedDate,
              }
              this.approved?.push(worker)
            }
            else {
              this.submitted?.push(worker)
            }
          }
        }
      }
    },
    async getMessages() {
      const approveRes = await api.getMessages({type: 'approve'})
      this.approveMessages = approveRes.data
      const rejectRes = await api.getMessages({type: 'reject'})
      this.rejectMessages = rejectRes.data
    },
    toggleSaveMessage(){ 
      this.saveMessage = !this.saveMessage
    },
    handleApprove({ workerID, assignmentID }: { workerID: string, assignmentID: string}): void {
      this.modalApproveIsVisible = true
      this.workerID = workerID
      this.assignmentID = assignmentID
    },
    handleReject(workerID: string): void {
      this.modalRejectIsVisible = true
      this.assignmentID = workerID
    },
    handleQualify(workerID: string): void {
      this.qualifyWorker(workerID)
    },
    handleQualifyAll(): void {
      if (this.submitted != null) {
        for (const worker of this.submitted) {
          this.qualifyWorker(worker.id)
        }
      }
      if (this.approved != null) {
        for (const worker of this.approved) {
          this.qualifyWorker(worker.id)
        }
      }
      if (this.rejected != null) {
        for (const worker of this.rejected) {
          this.qualifyWorker(worker.id)
        }
      }
    },
    async qualifyWorker(workerID: string): Promise<void> {
      // this.modalRejectIsVisible = true
      this.workerID = workerID
      const awardQualificationID = this.awardQualificationID || ''
      const res = await api.qualifyWorker({ awardQualificationID, workerID })

      if (res.success) {
        // await this.getWorkers()
        this.$toasted.show(res.message, {
          type: 'success',
          position: 'bottom-right',
          duration: 3000,
        })
      } else {
        this.$toasted.show(res.message, {
          type: 'error',
          position: 'bottom-right',
          duration: 3000,
        })
      }
    },
    closeModal(): any {
      this.feedback = ''
      this.assignmentID = ''
      this.modalApproveIsVisible = false
      this.modalRejectIsVisible = false
    },
    async approveAssignment(): Promise<any> {
      const id = this.assignmentID
      const feedback = this.approvalFeedback
      const awardQualificationID = this.awardQualificationID || ''
      const workerID = this.workerID

      const res = await api.approveAssignment({
        id,
        feedback,
        awardQualificationID,
        workerID,
      })
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
      if (res.success) {        
        this.closeModal()
        await this.getWorkers()

        this.$toasted.show(res.message, {
          type: 'success',
          position: 'bottom-right',
          duration: 3000,
        })
      } else {
        this.$toasted.show(res.message, {
          type: 'error',
          position: 'bottom-right',
          duration: 3000,
        })
      }    
    },
    async rejectAssignment(): Promise<any> {
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
      const id = this.assignmentID
      const feedback = this.rejectFeedback
      const res = await api.rejectAssignment({ id, feedback })

      if (res.success) {        
        this.closeModal()
        await this.getWorkers()

        this.$toasted.show(res.message, {
          type: 'error',
          position: 'bottom-right',
          duration: 3000,
        })
      } else {
        this.$toasted.show(res.message, {
          type: 'error',
          position: 'bottom-right',
          duration: 3000,
        })
      }
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
    async refreshPage(): Promise<void> {
      this.submitted = null
      this.approved = null
      this.rejected = null

      await this.getWorkers()
    },
  },
})
</script>
<style lang="scss">
.Workers {
  position: relative;

  > .BaseButton.is-prime {
    position: absolute;
    top: 0;
    right: 0;
  }
  > .BaseButton.is-second {
    position: absolute;
    right: 0;
  }
}
option {
  display: flex;
  justify-content: space-between;
}
.MessageSelect {
  width: 100%; 
  height: 30px;
}

</style>
