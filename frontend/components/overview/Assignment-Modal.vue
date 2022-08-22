<template>
  <div
    ref="modal"
    class="BaseModal"
    :class="{ 'is-visible': visible }"
    @click="closeModal"
  >
    <div class="BaseModal-Content">
      <h3 class="Title">{{ title }}</h3>
      <p v-if="type=='accept'">
        Fill in a list of AssignmentIDs you want to accept.
        The List should be seperated by any non-alphabetic character.
      </p>
      <p v-else>
        Fill in a list of AssignmentIDs you want to reject.
        The List should be seperated by any non-alphabetic character.
      </p>
      <slot></slot>
      <div class="BaseModal-Content-Buttons">
        <BaseButton
          second
          square
          :title="cancel.label"
          gray-light
          @click="onCancel"
        />
        <BaseButton
          prime
          square
          :title="accept.label"
          :green="accept.type === 'success'"
          :red="accept.type === 'warning'"
          @click="onAccept"
        />
      </div>
    </div>
  </div>
</template>
<script lang="ts">
import Vue from 'vue'

import BaseButton from '@/components/BaseButton.vue'
import BaseTextArea from '@/components/BaseTextarea.vue'
import BaseTextarea from '@/components/BaseTextarea.vue'
export default Vue.extend({
  name: 'BaseModal',
  components: {
    BaseButton,
    BaseTextArea,
    BaseTextarea
},
  props: {
    cancel: {
      type: Object,
      default: () => ({
        label: 'cancel',
      }),
    },
    accept: {
      type: Object,
      default: () => ({
        label: 'accept',
      }),
    },
    title: {
      type: String,
      default: '',
    },
    visible: {
      type: Boolean,
      default: false,
    },
    type: {
      type: String,
      default: ""
    }
  },
  data: () => ({}),
  methods: {
    onCancel(): void {
      this.$emit('onCancel')
    },
    onAccept():void {
      this.$emit('onAccept')
    },
    closeModal(e: any) {
      const target = e.target
      if (target === this.$refs.modal) {
        this.$emit('onCancel')
      }
    },
  },
})
</script>
<style lang="scss">
.BaseModal {
  position: relative;
  position: fixed;
  z-index: zIndex(modal);
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  background-color: rgba(black, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.3s $ease;
  opacity: 0;
  visibility: hidden;

  &.is-visible {
    opacity: 1;
    visibility: visible;

    .BaseModal-Content {
      transform: scale(1) translateZ(0);
    }
  }

  .BaseModal-Content {
    background-color: #fff;
    padding: 30px;
    width: 35vw;
    box-shadow: 5px 5px 0px 0 rgba(lighten(color(gray-dark), 10%), 0.75);
    transform: scale(0.85) translateZ(0);
    transition: all 0.3s $ease;
  }

  .Title {
    margin-top: 0;
  }

  p {
    line-height: 1.5;

    + * {
      margin-top: 40px;

      &:not(:last-child) {
        margin-bottom: 30px;
      }
    }
  }

  .BaseModal-Content-Buttons {
    display: flex;
    justify-content: flex-end;

    > * {
      margin-left: 10px;
    }
  }
}
</style>
