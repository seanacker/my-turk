<template>
  <table>
    <tr v-if="!workers" class="light">
      <td class="is-loading align-left" >WorkerID</td>
      <td class="is-loading align-left" >AssignmentID</td>
      <td class="is-loading align-left" >Started</td>
      <td class="is-loading align-left" >Finished</td>
      <td class="is-loading align-left" >Handle</td>
      <td v-if="isExperimentView" class="is-loading" :style="{textAlign: 'center'}">HITId</td>    
    </tr>
    <tr v-else light>
      <td class="align-left">WorkerID</td>
      <td class="align-left">AssignmentID</td>
      <td class="align-left">Started</td>
      <td class="align-left">Finished</td>
      <td class="align-left">Handle</td>
      <td class="align-left" v-if="isExperimentView">HITId</td>    
    </tr>

    <tr v-for="worker in workers" :key="worker.id">
      <td :style="{ fontSize:'12px'}">
        {{ worker.id }}&nbsp;<br/>
        <BaseCopy :value="worker.assignmentID" />
      </td>
      <td :style="{fontSize:'12px'}">
        {{ worker.assignmentID }}&nbsp;
        <BaseCopy :value="worker.assignmentID" />
      </td>

      <td class="align-right">
        <div>{{ worker.started.time }}</div>
        <div class="is-small">{{ worker.started.date }}</div>
      </td>

      <td class="align-right">
        <div>{{ worker.finished.time || '' }}</div>
        <div class="is-small">{{ worker.finished.date || '' }}</div>
      </td>
      <td class="align-right">
        <div :style="{display: 'flex', flexDirection: 'column'}">
          <button class="Anchor" @click="onApprove(worker.id, worker.assignmentID)">
            ACCEPT
          </button>
          <button class="Anchor" @click="onReject(worker.assignmentID)">
            REJECT
          </button>
          <button class="Anchor" @click="onQualify(worker.id)"
            >ASSIGN<br/> QUALIFICATION
          </button>
        </div>
      </td>
      <td>
        <span v-if="isExperimentView" class="Anchor" @click="onHitClick(worker.HITId, worker.awardQualificationId)"
          >{{ worker.HITId}}</span
        >
      </td>
    </tr>
  </table>
</template>
<script lang="ts">
import Vue from 'vue'

import BaseCopy from '@/components/BaseCopy.vue'
import BaseButton from '@/components/BaseButton.vue'

export default Vue.extend({
  name: 'TableWaiting',
  components: {
    BaseCopy,
    BaseButton
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
  data: () => ({
  }),
  methods: {
    onApprove(workerID: string, assignmentID: string) {
      this.$emit('onApprove', { workerID, assignmentID })
    },
    onReject(id: string) {
      this.$emit('onReject', id)
    },
    onQualify(id: string) {
      console.log('Qualify clicked')
      this.$emit('onQualify', id)
    },
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
  },
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
