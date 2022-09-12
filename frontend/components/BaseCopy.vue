<template>
  <div class="BaseCopy" @click="handleClick">
    <font-awesome icon="fa-light fa-copy"></font-awesome>
  </div>
</template>
<script>
import Vue from 'vue'

export default Vue.extend({
  name: 'Tags',
  components: {},
  props: {
    value: {
      type: String,
      default: '',
    },
  },
  data: () => ({}),
  methods: {
    handleClick() {
      navigator.clipboard.writeText(this.value)
      this.$toasted.show('copied', {
        position: 'bottom-right',
        duration: 2000,
      })
    },
  },
})
</script>
<style lang="scss">
.BaseCopy {
  position: relative;
  cursor: pointer;
  display: inline;
}
</style>
