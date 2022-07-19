<template>
  <div class="Table">
    <BaseRow v-if="!workers" light>
      <span class="is-loading is-wide">WorkerID</span>
      <span class="is-loading is-wide">AssignmentID</span>
      <span class="is-loading is-narrow align-right">Started</span>
      <span class="is-loading is-narrow align-right">Finished</span>
      <span class="is-loading align-right">Accept / Reject</span>
    </BaseRow>

    <BaseRow v-else light>
      <span class="">WorkerID</span>
      <span class="is-wide">AssignmentID</span>
      <span class="is-narrow align-right">Started</span>
      <span class="is-narrow align-right">Finished</span>
      <span class="align-right">Accept / Reject</span>
    </BaseRow>

    <BaseRow v-for="worker in workers" :key="worker.id">
      <span class="">
        {{ worker.id }}&nbsp;
        <BaseCopy :value="worker.assignmentID" />
      </span>
      <span class="is-wide">
        {{ worker.assignmentID }}&nbsp;
        <BaseCopy :value="worker.assignmentID" />
      </span>

      <span class="is-narrow align-right">
        <div>{{ worker.started.time }}</div>
        <div class="is-small">{{ worker.started.date }}</div>
      </span>

      <span class="is-narrow align-right">
        <div>{{ worker.finished.time || '' }}</div>
        <div class="is-small">{{ worker.finished.date || '' }}</div>
      </span>
      <span class="align-right">
        <span class="Anchor" @click="onApprove(worker.id, worker.assignmentID)"
          >accept</span
        >
        &nbsp;/&nbsp;
        <span class="Anchor" @click="onReject(worker.assignmentID)"
          >reject</span
        >
        &nbsp;/&nbsp;
        <span class="Anchor" @click="onQualify(worker.id)"
          >assign Qualification</span
        >
      </span>
    </BaseRow>
  </div>
</template>
<script lang="ts">
import Vue from 'vue'

import BaseCopy from '@/components/BaseCopy.vue'
import BaseRow from '@/components/BaseRow.vue'

export default Vue.extend({
  name: 'TableWaiting',
  components: {
    BaseCopy,
    BaseRow,
  },
  props: {
    workers: {
      type: Array,
      default: null,
    },
  },
  data: () => ({}),
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
  },
})
</script>
<style lang="scss">
.Table {
  display: flex;
  flex-wrap: wrap;

  [class*='fa-thumbs-'] {
    cursor: pointer;
  }
}
</style>
