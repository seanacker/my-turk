<template>
  <div class="Overview">
    <BaseHeadline
      :route="route"
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
        @expireAndDeleteHIT="expireAndDeleteHIT"
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
<script lang="ts">
import Vue from 'vue'

import BaseButton from '@/components/BaseButton.vue'
import BaseHeadline from '@/components/BaseHeadline.vue'
import BaseModal from '@/components/BaseModal.vue'
import BaseWrapper from '@/components/BaseWrapper.vue'
import Table from '@/components/overview/Table.vue'
import api from '@/api/index'
import { Experiment, Hit, Route } from '@/lib/types'

type OverviewData = {
  route: Route
  hit: Hit | undefined
  modalIsVisible: boolean
  experiments: { production: Experiment[], sandbox: Experiment[] }
  prodIsHidden: boolean
  sandIsHidden: boolean
}

export default Vue.extend({
  name: 'Tags',
  components: {
    BaseHeadline,
    BaseModal,
    BaseWrapper,
    BaseButton,
    Table,
  },
  data: (): OverviewData => ({
    route: {
      path: 'index',
      name: 'Click here to SignOut',
      params: { loggedOut: true },
    },
    modalIsVisible: false,
    experiments: { production: [], sandbox: []},
    prodIsHidden: false,
    sandIsHidden: false,
    hit: undefined
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
        console.log("data: ", result.data)
        this.experiments = result.data
        this.experiments.production = result.data.production || []
        this.experiments.sandbox = result.data.sandbox || []
        console.log('sandbox experiments: ', JSON.stringify(result.data.sandbox))
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
    async expireAndDeleteHIT(experiment: Experiment, hit: Hit) {
      const expireRes = await api.expireHIT({HITId: hit.HITId})
      if (expireRes.success) {
        this.$toasted.success(expireRes.message, {
          position: 'bottom-right',
          duration: 3000,
        })
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
      }
      else {
        this.$toasted.error(expireRes.message, {
            position: 'bottom-right',
            duration: 5000,
          })
      }

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
    closeModal() {
      this.modalIsVisible = false
    },
  },
})
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
