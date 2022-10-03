<template>
  <div class="BaseCheckbox">
    <input
      :id="hint"
      v-model="mValue"
      :checked="mValue"
      type="checkbox"
      @click="handleCheck"
    />
    <label class="Label">{{ label }}</label>
    <label class="Hint" :for="info">{{ info }}</label>
  </div>
</template>
<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'BaseCheckbox',
  props: {
    label: {
      type: String,
      default: '',
    },
    info: {
      type: String,
      default: '',
    },
    type: {
      type: String,
      default: '',
    },
    value: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    mValue: {
      get(): boolean {
        return this.value
      },
      set(value: boolean): void {
        // convert the label name to camelCase
        // eg:  AWS Access Key ID -> awsAccessKeyId
        let key = this.label.toLowerCase()
        key = key.replace(/ ([a-z])/g, (_: string, w: string) => w.toUpperCase())
        this.$emit('keyPress', {
          [key]: value,
        })
      },
    },
  },
  methods: {
    handleCheck(): void {
      const key = this.label.toLowerCase()

this.$emit('keyPress', {
        [key]: this.mValue,
      })
    },
  },
})
</script>
<style lang="scss">
.BaseCheckbox {
  background-color: color(bg);
  position: relative;
  padding: 10px 18px;
  width: 100%;
  display: flex;
  align-items: center;

  .Label {
    position: absolute;
    left: 18px;
    top: 10px;
    transform: translateY(-30px);
    font-size: rem(12px);
    transition: all 0.2s ease-out;
  }

  input,
  .Hint {
    outline: none;
    margin: 0;
    padding: 0;
    background-color: transparent;
    border: none;
    font-family: font(regular);
    font-size: rem(14px);
  }
  .Hint {
    padding-left: 15px;
    width: 100%;
    user-select: none;
  }
}
</style>
