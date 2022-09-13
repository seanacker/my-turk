<template>
  <table>
    <tr v-if="!workers" class="light">
      <td class="is-loading is-wide">WorkerID</td>
      <td class="is-loading is-wide">AssignmentID</td>
      <td class="is-loading is-narrow align-right">Started</td>
      <td class="is-loading is-narrow align-right">Finished</td>
      <td class="is-loading align-right">Approved</td>
      <td class="is-loading align-right" v-if="isExperimentView">HITId</td>      
    </tr>

    <tr v-else>
      <td class="">WorkerID</td>
      <td class="is-wide">AssignmentID</td>
      <td class="is-narrow align-right">Started</td>
      <td class="is-narrow align-right">Finished</td>
      <td class="is-narrow align-right">Approved</td>
      <td class="is-narrow align-right" v-if="isExperimentView">HITId</td>    
    </tr>

    <tr v-for="worker in workers" :key="worker.id">
      <td>
        {{ worker.id }}&nbsp;<br/>
        <BaseCopy :value="worker.id" />
      </td>
      <td>
        {{ worker.assignmentID }}&nbsp;
        <BaseCopy :value="worker.assignmentID" />
      </td>
      <td class="align-right">
        <div>{{ worker.started.time }}</div>
        <div class="is-small">{{ worker.started.date }}</div>
      </td>

      <td class="align-right">
        <div>{{ worker.finished.time || '' }}</div>
        <div>{{ worker.finished.date || '' }}</div>
      </td>

      <td class="align-right">
        <div>{{ worker.approved.time || '' }}</div>
        <div class="is-small">{{ worker.approved.date || '' }}</div>
      </td>
      <td v-if="isExperimentView" class="Anchor" @click="onHitClick(worker.HITId, worker.awardQualificationId)"
        >{{ worker.HITId}}</td
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
    BaseCopy
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
  .light {
      font-weight: 400;
      font-size: rem(14px);
      color: color(text-light);
    }
  button {
    background-color: #2e3035;
    color: white;
    text-decoration: none !important;
    border: 0;
    margin: 2px;
    border-radius: 10px;
    box-shadow: 0px 8px 15px rgba(0, 0, 0, 0.2);
    font-family: font(regular);
    padding: 4px;
  }
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
</style>
