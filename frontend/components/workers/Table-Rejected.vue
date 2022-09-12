<template>
  <table>
    <tr v-if="!workers" class="light">
      <td class="is-loading align-left" >WorkerID</td>
      <td class="is-loading align-left" :style="{overflowWrap: 'break-word'}">AssignmentID</td>
      <td class="is-loading align-left">Started</td>
      <td class="is-loading align-left">Finished</td>
      <td class="is-loading align-left">Rejected</td>
      <td v-if="isExperimentView" class="is-loading align-left">HITId</td>    
    </tr>
    <tr v-else light>
      <td class="align-left">WorkerID</td>
      <td class="align-left">AssignmentID</td>
      <td class="align-left">Started</td>
      <td class="align-left">Finished</td>
      <td class="align-left">Rejected</td>
      <td class="align-left" v-if="isExperimentView">HITId</td>    
    </tr>

    <tr v-for="worker in workers" :key="worker.id">
      <td>
        {{ worker.id }}&nbsp;
        <BaseCopy :value="worker.id" />
      </td>
      <td>
        {{ worker.assignmentID }}&nbsp;
        <BaseCopy :value="worker.assignmentID" />
      </td>
      <td>
        <div>{{ worker.started.time }}</div>
        <div class="">{{ worker.started.date }}</div>
      </td>
      <td>
        <div>{{ worker.finished.time }}</div>
        <div class="">{{ worker.finished.date }}</div>
      </td>
      <td>
        <div>{{ worker.rejected.time || '' }}</div>
        <div class="">{{ worker.rejected.date || '' }}</div>
      </td>
      <td v-if="isExperimentView" class="Anchor" @click="onHitClick(worker.HITId, worker.awardQualificationId)"
      >{{worker.HITId}}</td
      >
    </tr>
  </table>
</template>
<script lang="ts">
import Vue from 'vue'

import BaseCopy from '@/components/BaseCopy.vue'

export default Vue.extend({
  name: 'TableWaiting',
  components: {
    BaseCopy,
  },
  props: {
    workers: {
      type: Array,
      default: null,
    },
    isExperimentView: {
      type: Boolean,
      default: false
    }
  },
  data: () => ({}),
  methods: {
    async onHitClick(HITId: string, awardQualificationId: string) {
      await this.$router.push({
        name: 'Workers',
        params: {},
        query: {      
          HITId,
          awardQualificationId,
        },
      }).then(() => window.location.reload())      
    },
  }
})
</script>
<style lang="scss">
  table {
    border-spacing: 0 1em;
    width: 100%
  }
  tr {
    width: 100%;
    margin-top: 10px;
  }
  td {
    padding: 0 10px;
    font-size: 12px;
  }
</style>
