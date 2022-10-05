<template>
    <div class="BaseInput" :class="{ 'is-disabled': disabled }">
      <label class="TitleLabel">
        <b>{{ label }}</b>
      </label>
      <input
        v-model="mValue"
        :type="type"
        name
        :disabled="disabled"
        :placeholder="placeholder"
      />
      <label class="InfoLabel">
        <i v-html="info"></i>
      </label>
    </div>
</template>
<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'BaseInput',
  props: {
    label: {
      type: String,
      default: '',
    },
    type: {
      type: String,
      default: '',
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    value: {
      type: String,
      default: '',
    },
    placeholder: {
      type: String,
      default: '',
    },
    info: {
      type: String,
    }
  },
  data: () => ({}),
  computed: {
    mValue: {
      get(): string {
        return this.value
      },
      set(value: string): void {
        // convert the label name to camelCase
        // eg:  AWS Access Key ID -> awsAccessKeyId
        let key = this.label.toLowerCase()
        key = key.replace(/ ([a-z])/g, (_, w) => w.toUpperCase())
        this.$emit('keyPress', {
          [key]: value,
        })
      },
    },
  },
  methods: {
    handleKeyPress() {}
  },
})
</script>
<style lang="scss">
.BaseInput {
  background-color: color(bg);
  position: relative;
  padding: 10px 18px;
  width: 100%;
  display: flex; 
  flex-direction: column;
  margin-bottom: 20px;

  &.is-disabled {
    .Label {
      top: 20px;
    }
    input {
      color: white;
      background-color: #737373
    }    
  }
  .TitleLabel {
    position: absolute;
    left: 18px;
    top: 10px;
    transform: translateY(-30px);
    font-size: rem(12px);
    transition: all 0.2s ease-out;
    display: flex;
    flex-direction: row;
    align-items: baseline;
  }

  .InfoLabel {
    position: absolute;
    left: 18px;
    bottom: -50px;
    transform: translateY(-30px);
    font-size: rem(12px);
    transition: all 0.2s ease-out;
    display: flex;
    flex-direction: row;
    align-items: baseline;
  }

  input {
    outline: none;
    margin: 0;
    padding: 0;
    background-color: transparent;
    border: none;
    font-family: font(regular);
    width: 100%;
    font-size: rem(14px);
    &::placeholder {
      color: rgba(black, 0.4);
    }
    &:focus {
      + .Label {
        transform: translateY(-30px);
        font-size: rem(12px);
      }
    }
  }
}
</style>
