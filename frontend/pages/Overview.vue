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

      <div :style="{display: 'flex', flexDirection: 'column'}">
        <button @click="toggleHeaderMenu" class="headerMenuButton">
          <img v-if="showHeaderMenu" src="../public/closeicon.png"/>
          <img v-else src="../public/burgermenuicon.png"/>
        </button>
        <div v-if="showHeaderMenu" class="HeaderMenu">
          <BaseButton square second @click="addExperiment">new experiment</BaseButton>
          <BaseButton square second @click="refreshPage">refresh</BaseButton>
        </div>
      </div>
    </div>

    <BaseWrapper title="Production" red :hidden="prodIsHidden">
      <Table
        :experiments="experiments.production"
        @createHIT="createHIT"
        @onHitDeleteClick="handleDeleteHIT"
        @expireHIT="expireHIT"
        @deleteHIT="deleteHIT"
        @cancelHIT="cancelHIT"
      />
    </BaseWrapper>

    <BaseWrapper title="Sandbox" green :hidden="sandIsHidden">
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
    <AssignmentModal
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
      <BaseTextarea 
        label="AssignmentIDs"
        @keyPress="setAcceptIDs"
      ></BaseTextarea>
      <p>
        Approving these Assignments will cost you {{this.priceForAccepting}}$
      </p>
    </AssignmentModal>
    <AssignmentModal
      :visible="rejectAssignmentModalVisible"
      title="Reject Assignments"
      :rewardPerAssignment="rewardPerAssignment"
      :cancel="{ label: 'cancel' }"
      :accept="{ label: 'Reject', type: 'success' }"
      type="reject"
      @onAccept="rejectAssignments"
      @onCancel="closeModal"
    >
      <BaseTextarea 
        label="AssignmentIDs"
        @keyPress="setRejectIDs"
      ></BaseTextarea>

    </AssignmentModal>
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
import AssignmentModal from '@/components/overview/Assignment-Modal.vue'
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
}

export default Vue.extend({
  name: 'Tags',
  components: {
    BaseHeadline,
    BaseModal,
    BaseWrapper,
    BaseButton,
    Table,
    AssignmentModal,
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
    awardQualificationID: undefined
  }),
  mounted() {
    this.getExperiments()
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
    async createHIT(experiment: Experiment) {
      console.log(experiment)
      const res = await api.createHIT(experiment)
      console.log(res)

      if (res.success) {
        this.$toasted.success(res.message, {
          position: 'bottom-right',
          duration: 3000,
        })
        const hit = res.data.HIT
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
    async cancelHIT(experiment: Experiment, hit: Hit){
      //needs to be implemented in timed hit publication
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
      const HITId = hit.HITId
      const maxAssignments = hit.maxAssignments
      const available = hit.available
      const pending = hit.pending
      const completed = hit.completed
      const creationTime = hit.creationTime
      const title = hit.title
      const status = hit.status
      const waitingForApproval = 
        `${parseInt(maxAssignments) - parseInt(available) - parseInt(completed) - parseInt(pending)}`

      const mHIT = {
        HITId: HITId,
        title,
        available: `${available} / ${maxAssignments} `,
        pending: `${pending} / ${maxAssignments} `,
        waitingForApproval: `${waitingForApproval} / ${maxAssignments} `,
        completed: `${completed} / ${maxAssignments} `,
        creationTime,
        status,
        maxAssignments
      }

      if (!experiment.hits) {
        experiment.hits = []
      }
      experiment.hits.push(mHIT)
      return experiment
    },
    /////////////////////////////
    // needs to be implemented //
    /////////////////////////////

    // handleDeleteHIT(hit: Hit) {
    //   this.modalIsVisible = true
    //   this.hit = hit
    // },
    // async deleteHIT() {
    //   const id = this.hit?.HITId
    //   console.log(api)
    //   const res = await api.deleteHIT({ id })

    //   this.modalIsVisible = false
    //   this.hit = {}

    //   if (res.success) {
    //     this.getExperiments()

    //     this.$toasted.success(res.message, {
    //       position: 'bottom-right',
    //       duration: 3000,
    //     })
    //   } else {
    //     this.$toasted.error(res.message, {
    //       position: 'bottom-right',
    //       duration: 5000,
    //     })
    //   }
    // },

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
      const res = await api.approveAssignments({assignmentIds: this.acceptIDs, awardQualificationID: this.awardQualificationID})
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
      console.log("ids:", this.rejectIDs)
      const res = await api.rejectAssignments({assignmentIds: this.rejectIDs})
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
    }


  },
})
</script>
<style lang="scss">
.Overview {
  position: relative;

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
}
</style>
