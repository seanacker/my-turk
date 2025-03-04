<template>
  <div
    class="BaseButton"
    :class="[
      { 'is-prime': prime },
      { 'is-second': second },
      { 'is-square': square },
      { 'is-inline': inline },
      { 'is-red': red },
      { 'is-white': white},
      { 'is-green': green },
      { 'is-orange': orange },
      { 'is-gray-light': grayLight },
      { 'is-full-width': fullWidth},
      { 'no-border-right': noBorderRight },
      { 'no-border-bottom': noBorderBottom },
      { 'is-disabled': disabled}

    ]"
    :style="{backgroundColor: disabled ? 'grey' : white ? 'white' : '', marginRight: marginRight}"
    @click="disabled ? null : $emit('click')"
  >
    <slot></slot>
    <span :style="{whiteSpace: 'nowrap'}">{{ title }}</span>
  </div>
</template>
<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'BaseButton',
  props: {
    prime: {
      type: Boolean,
      default: false,
    },
    second: {
      type: Boolean,
      default: false,
    },
    title: {
      type: String,
      default: '',
    },
    square: {
      type: Boolean,
      default: false,
    },
    inline: {
      type: Boolean,
      default: false,
    },
    red: {
      type: Boolean,
      default: false,
    },
    green: {
      type: Boolean,
      default: false,
    },
    grayLight: {
      type: Boolean,
      default: false,
    },
    fullWidth: {
      type: Boolean,
      default: false
    },
    noBorderRight: {
      type: Boolean,
      default: false
    },
    white: {
      type: Boolean,
      default: false
    },
    orange: {
      type: Boolean,
      default: false
    },
    noBorderBottom: {
      type: Boolean, 
      default: false
    },
    marginRight: {
      type: Number,
      default: 0
    },
    disabled: {
      type: Boolean,
      default: false    
    }
  },
})
</script>
<style lang="scss">
.BaseButton {
  background-color: color(gray-dark);
  border-radius: 50px;
  text-transform: uppercase;
  padding: 16px 27px;
  font-size: rem(12px);
  color: color(text-white);
  display: inline-block;
  cursor: pointer;
  font-weight: 700;
  line-height: 1;
  transition: all 0.2s $ease;
  user-select: none;
  box-shadow: 0px 2px 20px 0 rgba(lighten(color(gray-dark), 10%), 0.4);
  font-family: font(regular);
  text-align: center;
  white-space: nowrap;

  &.no-border-right {
      border-right: 0 !important;
    }
  &.no-border-bottom {
    border-bottom: 0 !important;
  }

  &.is-full-width {
    width: 100%
  }

  &:hover {
    box-shadow: 0px 5px 20px 0 rgba(lighten(color(gray-dark), 10%), 0.5);
    transform: translateY(-3px);
  }

  &.is-disabled {
    &:hover {
      box-shadow: none !important;
      transform: none !important;
    }
  }

  &.is-square {
    padding: 12px 20px;
    border-radius: 0;
    font-size: rem(12px);

    &.is-prime {
      padding: 12px 40px;
    }
  }

  &.is-red {
    background-color: color(red);

    &:hover {
      box-shadow: 0px 5px 20px 0 lighten(color(red), 10%);
    }
  }
  &.is-white {
    background-color: white;

    &:hover {
      background-color: #CACBCC !important;
      transform: none !important;
    }
  }

  &.is-green {
    background-color: color(green);

    &:hover {
      box-shadow: 0px 5px 20px 0 rgba(color(green), 0.5);
    }
  }
  &.is-orange {
    background-color: color(orange);

    &:hover {
      box-shadow: 0px 5px 20px 0 rgba(color(orange), 0.5);
    }
  }

  &.is-gray-light {
    background-color: color(gray-light);
    color: black;

    &:hover {
      box-shadow: 0px 5px 20px 0 rgba(color(gray-light), 0.5);
    }
  }


  &.is-second {
    background-color: transparent;
    color: color(gray-dark);
    border: solid 2px color(gray-dark);

    &:hover {
      transform: translateY(0px);
      background-color: rgba(color(gray-dark), 0.25);
      box-shadow: none;
    }

    &.is-gray-light {
      color: lighten(color(gray-dark), 30%);
      box-shadow: none;
      border: none;

      &:hover {
        color: lighten(color(gray-dark), 0%);
        background-color: rgba(lighten(color(gray-dark), 30%), 0.1);
      }
    }

    &.is-red {
      color: color(red);
      border: none;

      &:hover {
        background-color: rgba(color(red), 0.1);
      }
    }

    &.is-green {
      color: color(green);
      border: none;

      &:hover {
        background-color: rgba(color(green), 0.1);
      }
    }
  }
}
</style>
