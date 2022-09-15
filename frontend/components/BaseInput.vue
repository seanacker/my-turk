<template>
  <div class="BaseInput" :class="{ 'is-disabled': disabled }">
    <input
      v-model="mValue"
      :type="type"
      name
      :disabled="disabled"
      :placeholder="placeholder"
    />
    <label class="Label">
      {{ label }}
      <div v-if="info" @mouseenter="toggleShowInfoText()" @mouseleave="toggleShowInfoText()">
        <div class="Icon"><fa icon="info"></fa></div>
        <p class="InfoText" v-if="showInfoText">
          {{info}}
        </p>
      </div>
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
  data: () => ({
    showInfoText: false
  }),
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
    handleKeyPress() {},
    toggleShowInfoText() {
      this.showInfoText = !this.showInfoText
    }
  },
})
</script>
<style lang="scss">
.BaseInput {
  background-color: color(bg);
  position: relative;
  padding: 10px 18px;
  width: 100%;

  &.is-disabled {
    background-color: lighten(color(bg), 5%);
    .Label {
      top: 20px;
    }
  }

  .Label {
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
  .InfoText {
    position: relative;
    background-color: yellow;
    color: black;
    border: 1px solid black;
    width: 300px;
  }
  .Icon {
    margin-left: 5px;
    margin-bottom: 2px;
    padding: 5px;
    line-height: -5px;
  }
}
</style>
