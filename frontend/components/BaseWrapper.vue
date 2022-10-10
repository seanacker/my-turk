<template>
  <div
    class="BaseWrapper"
    :class="[
      { 'is-red': red },
      { 'is-green': green },
      { 'is-gray-dark': grayDark },
      { 'is-gray-light': grayLight },
      { 'is-hidden': hidden },
    ]"
  >
    <div class="TitleWrapper" :style="{borderRadius: roundBorder ? '25px 25px 0 0 ' : '', display: mainTable ? 'flex' : '', justifyContent: 'space-between'}">
      <h2 :class="['Title', { 'is-main-Table' : mainTable}]" :stlye="{lineHeight: mainTable ? '50px !important' : ''}">{{ title }}</h2>
      <div class="buttonsWrapper" v-if="mainTable">
        <span style="margin-right: 20px;"><BaseButton @click="$emit('reload')"><fa  class="fa-lg" icon="arrows-rotate"/>&nbsp; &nbsp; REFRESH</BaseButton></span>
        <span ><BaseButton @click="$emit('addExperiment')"><fa  class="fa-lg" icon="plus"/>&nbsp; &nbsp; NEW EXPERIMENT</BaseButton></span>
      </div>
    </div>

    <slot></slot>
  </div>
</template>
<script lang='ts'>
import Vue from 'vue'
import BaseButton from './BaseButton.vue';

export default Vue.extend({
  name: 'BaseWrapper',
  components: {
    BaseButton,
},
  props: {
    title: {
      type: String,
      default: '',
    },
    items: {
      type: String,
      default: '',
    },
    red: Boolean,
    green: Boolean,
    grayDark: Boolean,
    grayLight: Boolean,
    hidden: {
      type: Boolean,
      default: false,
    },
    roundBorder: Boolean,
    mainTable: {
      type: Boolean,
      default: false
    }
  },
})
</script>
<style lang="scss">
.BaseWrapper {
  $outdent: 20px;
  background: #ffffff;
  box-shadow: 4px 20px 35px 0px rgba(0, 0, 0, 0.1);
  border-radius: 25px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  padding: 20px 40px;
  overflow: hidden;
  width: auto;
  margin-bottom: 60px;
  visibility: visible;

  &:last-of-type {
    margin-bottom: 0px;
  }

  &.is-red {
    > .TitleWrapper {
      background-color: color(red);
    }
    .Title {
      color: color(text-white);
    }
  }

  &.is-green {
    > .TitleWrapper {
      background-color: color(green);
    }

    .Title {
      color: color(text-white);
    }
  }

  &.is-hidden {
    visibility: hidden;
  }

  &.is-gray-light {
    .TitleWrapper {
      background-color: color(gray-light);
    }

    .Title {
      color: color(text);
    }
  }

  &.is-gray-dark {
    .TitleWrapper {
      background-color: color(gray-dark);
    }
    .Title {
      color: color(text-white);
    }
  }

  .TitleWrapper {
    width: calc(100% + 80px);
    transform: translate(-40px, -20px);
    padding: 10px 40px;
  }

  .Title {
    font-size: rem(20px);

    font-weight: 700;
  }

  .Table {
    width: calc(100% + #{$outdent * 2});
    transform: translateX(-$outdent);
  }

  .buttonsWrapper {
    display: flex; 
    flex-direction: row; 
    margin-top: 20px; 
    margin-bottom: 20px;
  }


  .is-main-Table{
    line-height: 50px;
    color: white;
  }

  @media (min-width: breakpoint(tablet-portrait)) {
    width: auto;
    transform: translateX(0);
    padding: 20px 40px;
  }
}
</style>
