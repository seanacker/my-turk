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
      :qualificationIDs="_qualificationIDs"
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
    <div class="saveButton">
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
        hint?: string,
        type?: string,
      }[]
    }[],
    _qualificationIDs: string
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
    }
  },
  data: (): SettingsData => ({
    _qualificationIDs: '',
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
            info: 'This is the experiment name for the overview page',
          },
        ],
      },
      {
        title: 'Informations for the workers',
        items: [
          {
            name: 'Title',
            value: 'This is the title shown to workers',
            info: 'This is the title shown to workers',
          },
          {
            name: 'Description',
            value: 'Tell me something about this experiment',
            info: 'This is the description shown to workers',
          },
          {
            name: 'Keywords',
            value: 'user test, data completion, information extraction',
            info: 'These can be searched for by potential participants. Please seperate them by comma',
          },
        ],
      },
      {
        title: 'Hit and Assignments',
        items: [
          {
            name: 'Hit expires after',
            value: '',
            info: 'e.g. 4 for 4 days or 0.5 for 12 hours',
          },
          {
            name: 'Assignment duration in minutes',
            value: '',
            info: 'e.g. 4 for 4 minutes or 0.5 for 30 seconds',
          },
          {
            name: 'Assignments per HIT',
            value: '',
            info: 'e.g. 9 for 9 assignments',
          },
        ],
      },
      {
        title: 'Qualification and Rewards',
        items: [
          {
            name: 'Award Qualification name',
            value: '',
            info: 'Fill in if you want to qualify your workers',
          },
          {
            name: 'Award Qualification description',
            value: '',
            info: 'Fill in if you want to qualify your workers',
          },
          {
            name: 'Award Qualification ID',
            value: '',
            disabled: true,
            info:
              'This is auto-generated from the qualification name and the qualification description by MTurk once the experiment is saved. It is the value used to ex- or include workers from HITs. ',
          },
          {
            name: 'Automaticaly assign Qualification',
            info: `Automaticaly assign the Qualification to workers that participated on it once a new hit is created.`,
            type: 'checkbox',
            value: false,
          },
          {
            name: 'Reward per Assignment',
            value: '',
            info: 'e.g. 3.5 for 3.5$',
          },
        ],
      },
      {
        title: 'Requirements and Entrypoint',
        items: [
          {
            name: 'Guard Hit by additional QualificationIDs',
            value: '',
            info: 'Please type in the Qualification IDs a Worker needs to participate on the HITs of this experiment. Seperate different IDs with a comma.',
          },
          {
            name: 'Exclude Workers by QualificationID',
            value: '',
            info: 'Please type in any additional Qualification IDs on which to exclude workers from HITs of this experiment. Seperate different IDs with a comma.',
          },
          {
            name: 'Default Requirements',
            info: 'Workers must be US-based,have 95% approval and more than 1.000 approved hits.',
            type: 'checkbox',
            value: false,
          },
          {
            name: 'Guard Hit by Qualification',
            info: `Make the qualification ID from this experiment an exclusion criteria for every HIT of the experiment.`,

            type: 'checkbox',
            value: false,
          },
          {
            name: 'Entrypoint',
            value: '',
            info: 'URL of the HITs of your Experiment (iframe)',
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
  beforeMount() {
    this._qualificationIDs = this.$route.params.qualificationIDs
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
      console.log("settings: ", settings)
      if (!this.validateSettings(settings)) {
        this.$toasted.error('Please fill out all required fields', {
          position: 'bottom-right',
          duration: 5000,
        })
      }
      else {
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
      }
    },
    validateSettings(settings: Partial<Experiment>) {
      let isValid = true
      if (!settings.assignmentDurationInMinutes) {
        this.$toasted.error(
          'Please pass a value for the assignment duration.', {
            position: 'bottom-right',
            duration: 5000
          }
        )
        isValid = false
      }      
      if (!settings.assignmentsPerHit) {
        this.$toasted.error(
          'Please pass a value for the assignments per Hit.', {
            position: 'bottom-right',
            duration: 5000
          }
        )
        isValid = false
      }
      if (!settings.description ) {
        this.$toasted.error(
          'Please pass a value for the description.', {
            position: 'bottom-right',
            duration: 5000
          }
        )
        isValid = false
      }
      if (!settings.endpoint) {
        this.$toasted.error(
          'Please pass a value for the endpoint.', {
            position: 'bottom-right',
            duration: 5000
          }
        )
        isValid = false
      }
      if (!settings.experimentName ) {
        this.$toasted.error(
          'Please pass a value for the experiment Name.', {
            position: 'bottom-right',
            duration: 5000
          }
        )
        isValid = false
      }
      if (!settings.hitExpiresAfter ) {
        this.$toasted.error(
          'Please pass a date for the expiration of the HIT.', {
            position: 'bottom-right',
            duration: 5000
          }
        )
        isValid = false
      }
      if (!settings.title ) {
        this.$toasted.error(
          'Please pass a value for the title of the HIT.', {
            position: 'bottom-right',
            duration: 5000
          }
        )
        isValid = false
      }
      if (!settings.rewardPerAssignment ) {
        this.$toasted.error(
          'Please pass a value for the reward per Assignment.', {
            position: 'bottom-right',
            duration: 5000
          }
        )
        isValid = false
      }
      return isValid
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
  .saveButton{
    position: relative;
    bottom: 20px;
  }
}
</style>
