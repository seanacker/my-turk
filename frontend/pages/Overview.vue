<template>
  <div class="Overview">
    <BaseHeadline
      :route="backbutton"
      prime
      title="myTurk"
      description="Overview of your experiments"
    />

    <BaseWrapper title="Production" red :hidden="prodIsHidden">
      <Table
        :experiments="experiments.production"
        @createHIT="createHIT"
        @onHitDeleteClick="handleDeleteHIT"
      />
    </BaseWrapper>

    <BaseWrapper title="Sandbox" green :hidden="sandIsHidden">
      <Table
        :experiments="experiments.sandbox"
        @createHIT="createHIT"
        @onHitDeleteClick="handleDeleteHIT"
      />
    </BaseWrapper>
    <BaseButton
      prime
      style="right: 14em"
      title="refresh"
      @click="refreshPage"
    />
    <BaseButton prime title="new experiment" @click="addExperiment" />

    <BaseModal
      :visible="modalIsVisible"
      title="Delete HIT"
      :cancel="{ label: 'cancel' }"
      :accept="{ label: 'delete', type: 'warning' }"
      @onAccept="deleteHIT"
      @onCancel="closeModal"
    >
      <p>Are you sure you want to delete this HIT?</p>
    </BaseModal>
  </div>
</template>
<script>
import BaseButton from '@/components/BaseButton.vue'
import BaseHeadline from '@/components/BaseHeadline.vue'
import BaseModal from '@/components/BaseModal.vue'
import BaseWrapper from '@/components/BaseWrapper.vue'
import Table from '@/components/overview/Table.vue'
import api from '@/api'

export default {
  name: 'Tags',
  components: {
    BaseHeadline,
    BaseModal,
    BaseWrapper,
    BaseButton,
    Table,
  },
  data: () => ({
    backbutton: {
      paths: 'index',
      name: 'Click here to SignOut',
      params: { loggedOut: true },
      hit: {},
    },
    modalIsVisible: false,
    experiments: {},
    prodIsHidden: false,
    sandIsHidden: false,
  }),
  mounted() {
    this.getExperiments()
  },

  methods: {
    async getExperiments() {
      const result = await api.getExperiments({ groupBy: 'endpoint' })
      console.log(result)
      this.prodIsHidden = result.endpoint === 'sandbox'
      console.log(this.prodIsHidden)
      this.sandIsHidden = result.endpoint === 'production'
      console.log(this.sandIsHidden)
      if (result.success) {
        this.experiments = result.data
        this.experiments.production = result.data.production || []
        this.experiments.sandbox = result.data.sandbox || []
        console.log('sandbox experiments: ', result.data.sandbox)
      }
    },
    async addExperiment() {
      const res = await api.addExperiment()

      if (res.success) {
        this.$router.push({
          name: 'Settings',
          params: { addExperiment: true },
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
    async createHIT(experiment) {
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
    addHITtoExperiment(experiment, hit) {
      const hitID = hit.HITId
      const maxAssignments = hit.MaxAssignments
      const available = hit.NumberOfAssignmentsAvailable
      const pending = hit.NumberOfAssignmentsPending
      const completed = hit.NumberOfAssignmentsCompleted
      const creationTime = hit.CreationTime
      const title = hit.Title
      const status = hit.HITStatus
      const waitingForApproval =
        maxAssignments - available - completed - pending

      const mHIT = {
        id: hitID,
        title,
        available: `${available} / ${maxAssignments} `,
        pending: `${pending} / ${maxAssignments} `,
        waitingForApproval: `${waitingForApproval} / ${maxAssignments} `,
        completed: `${completed} / ${maxAssignments} `,
        creationTime,
        status,
      }

      if (!experiment.hits) {
        experiment.hits = []
      }
      experiment.hits.push(mHIT)
      return experiment
    },
    handleDeleteHIT(hit) {
      this.modalIsVisible = true
      this.hit = hit
    },
    async deleteHIT() {
      const id = this.hit.id
      console.log(api)
      const res = await api.deleteHIT({ id })

      this.modalIsVisible = false
      this.hit = {}

      if (res.success) {
        this.getExperiments()

        this.$toasted.success(res.message, {
          position: 'bottom-right',
          duration: 3000,
        })
      } else {
        this.$toasted.error(res.message, {
          position: 'bottom-right',
          duration: 5000,
        })
      }
    },
    closeModal() {
      this.modalIsVisible = false
    },
  },
}
</script>
<style lang="scss">
.Overview {
  position: relative;

  > .BaseButton.is-prime {
    position: absolute;
    top: 0;
    right: 0;
  }
}
</style>
