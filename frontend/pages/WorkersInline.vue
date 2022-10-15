<template>
  <div class="WorkersInline collapsible-content">
    <BaseWrapper title="Workers waiting for approval" gray-dark>
      <TableSubmitted
        :workers="submitted"
        @onApprove="handleApprove"
        @onReject="handleReject"
        @onQualify="handleQualify"
      />
    </BaseWrapper>
    <BaseWrapper title="Workers approved" green>
      <TableApproved :workers="approved" />
    </BaseWrapper>
    <BaseWrapper title="Workers rejected" red>
      <TableRejected :workers="rejected" />
    </BaseWrapper>
    <div style="text-align: right">
      <BaseButton
        second
        title="Qualify All"
        :class="'QualifyBtn ' + experimentID "
        @click="handleQualifyAll"
      />
    </div>

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
        label="Please fill in the Message you want to leave for the worker"
        :style="{marginBottom: '15px', marginTop: '50px'}"
        :onSave="true"
        :value="approvalFeedback"
        @keyPress="setApprovalFeedback"
        @toggleSaveMessage="toggleSaveMessage()"
      />
      <p :style="{margin: '0 0  10px 0'}">or select a message</p>
      <select :style="{margin: '0 0 15px 0', width: '100%', overflow: 'hidden'}" class="MessageSelect">
        <option 
          value="" 
          disabled 
          selected 
          v-text="approveMessages ? 'Select your message!' : 'No messages saved!'">
        </option>
        <option v-for="(option, i) in approveMessages" :key="i" :value="option.message">
          {{ option.message }}
        </option>
        
      </select>
    </BaseModal>

    <BaseModal
      :visible="modalRejectIsVisible"
      title="Reject Assignment"
      :cancel="{ label: 'cancel' }"
      :accept="{ label: 'reject', type: 'warning' }"
      :onSave="true"
      @onAccept="rejectAssignment"
      @onCancel="closeModal"
      @toggleSaveMessage="toggleSaveMessage()"
    >
      <BaseTextarea
        name="feedback"
        label="Please fill in the Message you want to leave for the worker"
        :style="{marginBottom: '15px', marginTop: '50px'}"
        :value="rejectFeedback"
        @keyPress="setRejectionFeedback"
      />
      <p :style="{margin: '0 0  10px 0'}">or select a message</p>
      <select :style="{margin: '0 0 15px 0' ,width: '100%', overflow: 'hidden'}" class="MessageSelect">
        <option 
          value="" 
          disabled 
          selected 
          v-text="approveMessages ? 'Select your message!' : 'No messages saved!'">
        </option>
        <option v-for="(option, i) in rejectMessages" :key="i" :value="option.message">
          {{ option.message }} 
        </option>
      </select>
    </BaseModal>
  </div>
</template>
<script lang="ts">
import Moment from 'moment'
import Vue from 'vue'

import BaseButton from '@/components/BaseButton.vue'
import BaseModal from '@/components/BaseModal.vue'
import BaseTextarea from '@/components/BaseTextarea.vue'
import BaseWrapper from '@/components/BaseWrapper.vue'
import TableApproved from '@/components/workers/Table-Approved.vue'
import TableRejected from '@/components/workers/Table-Rejected.vue'
import TableSubmitted from '@/components/workers/Table-Submitted.vue'
import api from '@/api'
import { WorkersData, Worker } from '@/lib/types'

export default Vue.extend({
  name: 'Tags',
  components: {
    BaseButton,
    BaseModal,
    BaseTextarea,
    BaseWrapper,
    TableApproved,
    TableRejected,
    TableSubmitted,
  },
  props: {
    experimentID: {
      type: String,
      default: ''
    },
    HITId: {
      type: String,
      default: '',
    },
    awardid: {
      type: String,
      default: '',
    },
  },
  data: (): Partial<WorkersData> => ({
    title: '',
    creationTime: '',
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
    rejectMessages: []
  }),
  async mounted(): Promise<void> {
    await this.getMessages()
    await this.getWorkers()
  },
  methods: {
    clearWorkers(): void {
      this.submitted = []
      this.approved = []
      this.rejected = []
    },
    async getWorkers(): Promise<void> {
      const HITId = this.HITId

      const res = await api.listAssignments({ HITId })
      this.clearWorkers()

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
            HITId: HITId as string,
            id,
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
    },
    handleApprove({ workerID, assignmentID }: { workerID: string, assignmentID: string}): void {
      this.modalApproveIsVisible = true
      this.workerID = workerID
      this.assignmentID = assignmentID
    },
    handleReject(id: string): void {
      this.modalRejectIsVisible = true
      this.assignmentID = id
    },
    handleQualify(workerID: string): void {
      this.qualifyWorker(workerID)
    },
    handleQualifyAll(): void {
      if (this.submitted != null){
        for (const worker of this.submitted) {
          this.qualifyWorker(worker.id)
        }
      }
      if (this.approved != null){
        for (const worker of this.approved) {
          this.qualifyWorker(worker.id)
        }
      }
      if (this.rejected != null){
        for (const worker of this.rejected) {
          this.qualifyWorker(worker.id)
        }
      }
    },
    async qualifyWorker(workerID: string): Promise<void> {
      // this.modalRejectIsVisible = true
      this.workerID = workerID
      const awardid = this.awardid || ''

      const res = await api.qualifyWorker({
        awardQualificationID: awardid,
        workerID,
      })

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
    closeModal(): void {
      this.feedback = ''
      this.assignmentID = ''
      this.modalApproveIsVisible = false
      this.modalRejectIsVisible = false
    },
    async approveAssignment(): Promise<void> {
      const id = this.assignmentID
      const feedback = this.approvalFeedback
      const awardid = this.awardid || ''
      const workerID = this.workerID

      const res = await api.approveAssignment({
        id,
        feedback,
        awardid,
        workerID,
      })

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
    async rejectAssignment(): Promise<void> {
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
    setApprovalFeedback(val: any): void {
      this.approvalFeedback = val.feedback
    },
    setRejectionFeedback(val: any): void {
      this.rejectFeedback = val.feedback
    },
    async refreshPage(): Promise<void> {
      this.submitted = null
      this.approved = null
      this.rejected = null

      await this.getWorkers()
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
  },
})
</script>
<style lang="scss">
.WorkersInline {
  position: relative;
  flex-basis: 100%;

  > .BaseButton.is-prime {
    visibility: hidden;
  }
  > div > .BaseButton.is-second {
    background-color: white;
    margin-bottom: 10px;
    margin-right: 20px;
  }
  .BaseWrapper {
    margin: 20px;
  }
  .BaseWrapper .TitleWrapper {
    padding: 0px 40px;
  }
  .BaseModal > div > h3.Title {
    color: black;
  }
}
</style>
