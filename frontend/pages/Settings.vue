<template>
  <div class="Settings">
    <BaseHeadline
      :route="route"
      :title="settings.internalName || 'Add experiment'"
      :description="`You are in ${settings.endpoint} mode`"
    />

    <Container
      :groups="settingsInput"
      :endpoint="settings.endpoint"
      :has-hits="experiment.hits.length > 0"
      @updateSettings="updateSettings"
    />

    <div class="ButtonWrapper">
      <BaseButton
        second
        square
        title="Delete Experiment"
        :red="true"
        @click="handleDelete"
      />
      <BaseButton
        prime
        square
        title="Save Settings"
        :green="true"
        @click="handleSave"
      />
    </div>
    <BaseModal
      :visible="modalIsVisible"
      title="Delete Experiment"
      :cancel="{ label: 'cancel' }"
      :accept="{ label: 'delete', type: 'warning' }"
      @onAccept="deleteExperiment"
      @onCancel="closeModal"
    >
      <p>Are you sure you want to delete the experiment?</p>
    </BaseModal>
  </div>
</template>
<script lang="ts">
import Vue from 'vue'

import BaseButton from '@/components/BaseButton.vue'
import BaseHeadline from '@/components/BaseHeadline.vue'
import BaseModal from '@/components/BaseModal.vue'
import Container from '@/components/settings/Container.vue'
import api from '@/api'
import { Experiment } from '@/lib/types'

type SettingsData =  {
    modalIsVisible: boolean,
    route: {
      path: string,
      name: string,
    },
    settings: Partial<Experiment>,
    settingsInput: {title: string, items: 
      {
        name: string, 
        value: string | boolean, 
        disabled?: boolean, 
        info?: string,
        type?: string,
        placeholder?: string
      }[]
    }[]
}

export default Vue.extend({
  name: 'Tags',
  components: {
    BaseButton,
    BaseHeadline,
    BaseModal,
    Container,
  },
  props: {
    experiment: {
      type: Object,
      default() {
        return {
          experimentName: '',
          title: '',
          description: '',
          entrypoint: '',
          available: '0 / 0',
          pending: '0 / 0',
          waitingForApproval: '0 / 0',
          completed: '0 / 0',
          keywords: '',
          awardQualificationName: '',
          awardQualificationDescription: '',
          awardQualificationId: '',
          hitExpiresAfterDays: '',
          assignmentDurationInMinutes: '',
          assignmentsPerHit: '',
          defaultRequirements: false,
          guardHitByQualification: false,
          rewardPerAssignment: '',
          hits: [],
          endpoint: 'sandbox',
        }
      },
    },
    addExperiment: Boolean,
    initial: {
      type: Boolean,
      default: true,
    },
  },
  data: (): SettingsData => ({
    modalIsVisible: false,
    route: {
      path: 'Overview',
      name: 'back to Overview',
    },
    settings: {},
    settingsInput: [
      {
        title: 'Internal Settings',
        items: [
          {
            name: 'Experiment Name',
            value: '',
            placeholder: 'This is the experiment name for the overview page',
          },
        ],
      },
      {
        title: 'Informations for the workers',
        items: [
          {
            name: 'Title',
            value: 'This is the title shown to workers',
            placeholder: 'This is the title shown to workers',
          },
          {
            name: 'Description',
            value: 'Tell me something about this experiment',
            placeholder: 'This is the description shown to workers',
          },
          {
            name: 'Keywords',
            value: 'user test, data completion, information extraction',
            placeholder: 'seperated by comma',
          },
        ],
      },

      {
        title: 'Qualification and Rewards',
        items: [
          {
            name: 'Award Qualification name',
            value: '',
            placeholder: 'Fill in if you want to qualify your workers',
          },
          {
            name: 'Award Qualification description',
            value: '',
            placeholder: 'Fill in if you want to qualify your workers',
          },
          {
            name: 'Award Qualification ID',
            value: '',
            disabled: true,
            placeholder:
              'auto-generated from qualification name and qualification description',
          },
          {
            name: 'Reward per Assignment',
            value: '',
            placeholder: 'e.g. 3.5 for 3.5$',
          },
        ],
      },
      {
        title: 'Hit and Assignments',
        items: [
          {
            name: 'Hit expires after (days)',
            value: '',
            placeholder: 'e.g. 4 for 4 days or 0.5 for 12 hours',
          },
          {
            name: 'Assignment duration in minutes',
            value: '',
            placeholder: 'e.g. 4 for 4 minutes or 0.5 for 30 seconds',
          },
          {
            name: 'Assignments per HIT',
            value: '',
            placeholder: 'e.g. 9 for 9 assignments',
          },
        ],
      },
      {
        title: 'Requirements and Entrypoint',
        items: [
          {
            name: 'Default Requirements',
            info: 'US-based, 95% approval, more than 1.000 hits',
            type: 'checkbox',
            value: false,
          },
          {
            name: 'Guard Hit by Qualification',
            info: 'Make the above qualification an exclusion criteria for this HIT',
            type: 'checkbox',
            value: false,
          },
          {
            name: 'Guard Hit by additional QualificationIDs',
            value: '',
            info: 'Please type in the QualificationIDs a Worker needs to participate on the HIT. Seperate different IDs with a comma.',
          },
          {
            name: 'Exclude Workers by QualificationID',
            value: '',
            info: 'Please type in any additional QualificationIDs on which to exclude workers from the HIT. Seperate different IDs with a comma.',
          },
          {
            name: 'Entrypoint',
            value: '',
            placeholder: 'URL of your Experiment (iframe)',
          },
        ],
      },
    ],
  }),
  watch: {
    experiment: {
      immediate: true,
      handler(experiment) {
        this.settings = experiment
      },
    },
  },
  mounted() {
    this.loadExperimentSettings()
  },
  methods: {
    updateSettings(settings: Partial<Experiment>) {
      this.settings = Object.assign(this.settings, settings)
    },
    hyphensToCamelCase(value: string) {
      // if(!value) {return}
      let key = value.toLowerCase()
      key = key.replace(/ ([a-z])/g, (_, w) => w.toUpperCase())
      return key
    },
    async handleSave() {
      const id = this.$route.query.id || ''
      const settings = this.settings
      if (settings.guardHitByAdditionalQualificationids){
        settings.guardHitByAdditionalQualificationids = (settings.guardHitByAdditionalQualificationids as string).split(",")
      }
      if (settings.excludeWorkersByQualificationid){
        settings.excludeWorkersByQualificationid = (settings.excludeWorkersByQualificationid as string).split(",")
      }
      console.log(settings)

      const res = await api.saveSettings({ id, experiment: settings })

      if (res.success) {
        this.$toasted.success(res.message, {
          position: 'bottom-right',
          duration: 3000,
        })
        this.$router.push({ name: 'Overview' })
      } else {
        this.$toasted.error(res.message, {
          position: 'bottom-right',
          duration: 5000,
        })
      }
    },
    handleDelete() {
      this.modalIsVisible = true
    },
    closeModal() {
      this.modalIsVisible = false
    },
    async deleteExperiment() {
      const id = this.$route.query.id || ''
      const res = await api.deleteExperiment({ id })

      if (res.success) {
        this.$router.push({ name: 'Overview' })
        this.$toasted.error(res.message, {
          position: 'bottom-right',
          duration: 3000,
        })
      }
    },
    async loadExperimentSettings() {
      console.log("this: ",this)
      const id = this.$route.query.id     
      if (!this.addExperiment && this.initial && id) {
        const result = await api.getExperiments({ id })
        console.log('result', result)

        if (result.success && result.data[0]) {
          this.settings = result.data[0]
        }
      }
      this.settingsInput.map((elem) => {
        return elem.items.forEach((item) => {
          const key = this.hyphensToCamelCase(item.name) as string
          // @ts-ignore
          item.value = this.settings[key]
          return item
        })
      })
    },
  },
})
</script>
<style lang="scss">
.Settings {
  position: relative;

  .ButtonWrapper {
    top: 0;
    right: 0;
    padding-bottom: 30px;
    display: flex;
    justify-content: flex-end;
    position: absolute;
    flex-direction: column;
  }

  .BaseButton.is-prime {
    margin-left: 10px;
    margin-top: 0;
  }
  @media (min-width: breakpoint(tablet-portrait)) {
    .ButtonWrapper {
      flex-direction: row;
    }
  }
}
</style>
