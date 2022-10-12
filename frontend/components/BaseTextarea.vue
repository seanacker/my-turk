<template>
  <div class="BaseTextarea">
    <textarea v-model="mValue" :type="type" name></textarea>
    <label class="Label">{{ label }}</label>
    <div v-if="onSave" class="Save">
      <p 
        :style="{marginTop: '0', lineHeight: '1', marginRight: '5px'}">save this Message</p>
      <input
        type="checkbox"
        :style="{marginTop: '0', padding: '0', width: 'auto', backgroundColor: 'transparent', display: 'block', marginBottom: '10px'}"
        :value="false"
        @click="toggleSaveMessage()"
      />
    </div>
  </div>
</template>
<script lang="ts">
import Vue from 'vue'
import BaseCheckbox from './BaseCheckbox.vue'

export default Vue.extend({
    name: "BaseTextarea",
    props: {
        name: String,
        label: {
            type: String,
            default: "",
        },
        type: {
            type: String,
            default: "",
        },
        value: {
            type: String,
            default: "",
        },
        onSave: {
            type: Boolean,
            default: false
        }
    },
    data: () => ({}),
    computed: {
        mValue: {
            get(): string {
                return this.value;
            },
            set(value: string): void {
                // convert the label name to camelCase
                // eg:  AWS Access Key ID -> awsAccessKeyId
                let key = this.name.toLowerCase();
                key = key.replace(/ ([a-z])/g, (_, w) => w.toUpperCase());
                this.$emit("keyPress", {
                    [key]: value,
                });
            },
        },
    },
    components: { BaseCheckbox },
    methods: {
      toggleSaveMessage() {
        this.$emit('toggleSaveMessage')
      }
    }
})
</script>
<style lang="scss">
.BaseTextarea {
  background-color: color(bg);
  position: relative;
  width: 100%;
  margin: 20px 0;

  .Label {
    position: absolute;
    left: 14px;
    top: 10px;
    transform: translateY(-30px);
    font-size: rem(12px);
    transition: all 0.2s ease-out;
    font-weight: bold;
  }

  .Save {
    position: absolute;
    right: 0;
    top: 10px;
    transform: translateY(-30px);
    font-size: rem(12px);
    transition: all 0.2s ease-out;
    display: flex;
  }

  textarea {
    outline: none;
    border: 0;
    margin: 0;
    padding: 10px;
    border-color: color(gray-dark);
    background-color: transparent;
    resize: none;
    font-family: font(regular);
    width: 100%;
    font-size: rem(14px);
    height: 100px;
    display: block;
    line-height: 1.5;

    &:focus {
      + .Label {
        transform: translateY(-30px);
        font-size: rem(12px);
      }
    }
  }
}
</style>
