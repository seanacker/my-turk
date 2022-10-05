<template>
  <BaseWrapper
    title="Experiment Settings"
    :green="modes.sandbox"
    :red="modes.production"
  >
    <div class="Container">
      <div v-for="(group, index) in groups" :key="index" class="Groups">
        <h3           
          class="GroupTitle" 
          v-html="group.title"></h3>
        <i v-if="group.title == 'Requirements and Entrypoint'"  v-html="'<br/><i>Your Existing Experiments have the following qualification IDs: <br/> </br>' + parsedQualificationIDs + '</i></br>'" style="padding-left: 20px;"></i>
        <template v-for="(item, i) in group.items">
          <BaseCheckbox
            v-if="item.type && item.type === 'checkbox'"
            :key="i + '-checkbox'"
            :label="item.name || item"
            :value="item.value"
            :info="item.info"
            :hint="item.hint"
            @keyPress="handleKeyPress"
          />
          <BaseInput
            v-else
            :key="i + '-input'"
            :label="item.name || item"
            :value="item.value"
            :disabled="item.disabled"
            :info="item.info"
            :placeholder="item.placeholder || ''"
            @keyPress="handleKeyPress"
          />
        </template>
      </div>
    </div>
  </BaseWrapper>
</template>
<script lang="ts">
import Vue from 'vue'

import BaseCheckbox from '@/components/BaseCheckbox.vue'
import BaseInput from '@/components/BaseInput.vue'
import BaseWrapper from '@/components/BaseWrapper.vue'
import { Experiment } from '@/lib/types'

type SettingsContainerData = {
  settings: Partial<Experiment>,
    parsedQualificationIDs: string,
  modes: {
    sandbox: boolean,
    production: boolean,
  },
  options: {
    value: string,
    label: string,
    isSelected: boolean,
  }[],
}

export default Vue.extend({
  name: 'Grid',
  components: {
    BaseCheckbox,
    BaseInput,
    BaseWrapper,
  },
  props: {
    groups: {
      type: Array,
      default: () => [],
    },
    endpoint: {
      type: String,
      default: '',
    },
    hasHits: {
      type: Boolean,
      default: false,
    },
    qualificationIDs: {
      type: String,
      default: ''
    }
  },
  data: (): SettingsContainerData => ({
    settings: {},
    parsedQualificationIDs: '',
    modes: {
      sandbox: true,
      production: false,
    },
        options: [
      {
        value: 'https://mturk-requester-sandbox.us-east-1.amazonaws.com',
        label: 'Sandbox',
        isSelected: true,
      },
      {
        value: 'https://mturk-requester.us-east-1.amazonaws.com',
        label: 'Production',
        isSelected: false,
      },
    ],
  }),
  watch: {
    endpoint: {
      immediate: true,
      handler: function(val) {
        this.options.forEach(o => {
          o.value === val ? (o.isSelected = true) : (o.isSelected = false)
        })
        for (const mode in this.modes) {
          // @ts-ignore
          this.modes[mode] = false
        }
        // @ts-ignore
        this.modes[val] = true
      },
    },
  },
  beforeMount() {
    this.parsedQualificationIDs = this.qualificationIDs.split(';').join(' <br/>')
  },
  methods: {
    handleKeyPress(option: any) {
      Object.assign(this.settings, option)
      this.$emit('updateSettings', this.settings)
    },
  },
})
</script>
<style scoped lang="scss">
  i {
    font-size: 12px
  }
.Container {
  position: relative;
  transform: translateX(-20px);
  width: calc(100% + 20px);
  padding: 10px 0 20px;

  .Groups {
    display: grid;
    grid-template-columns: 50% 50%;
    grid-gap: 34px 20px;
    margin-bottom: 50px;
  }
  .GroupTitle {
    grid-column-start: 1;
    grid-column-end: 3;
    margin-left: 17px;
    margin-bottom: 5px;
    margin-top: 0px;
  }
  .BaseInput,
  .BaseCheckbox {
    grid-column-start: 1;
    grid-column-end: 3;
  }
}
</style>
