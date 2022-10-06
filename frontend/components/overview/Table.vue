<template>
  <div >
    <table :style="{
      minHeight: modalIsVisible ? '500px' : '', 
      transition: 'all 0.5s ease-out',
      }">
      <tr v-if="!experiments" light>
        <td class="is-loading">Title</td>
        <td class="is-loading">Details</td>
        <td class="is-loading align-center">Available</td>
        <td class="is-loading align-center">Pending</td>
        <td class="is-loading align-center">Waiting</td>
        <td class="is-loading align-center">Completed</td>
        <td class="is-loading">blub</td>
      </tr>

      <tr v-else light>
        <td>Title</td>
        <td>Details</td>
        <td class="align-center">Available</td>
        <td class="align-center">Pending</td>
        <td class="align-center">Waiting<br/> for<br/> approval</td>
        <td class="align-center">Completed</td>
        <td></td>
      </tr>
      <template v-for="experiment in experiments" bold>
        <BaseModal
            :visible="modalIsVisible==experiment._id"
            :key="experiment._id + 'Emailmodal'"
            title="Send Email"
            :cancel="{ label: 'cancel' }"
            :accept="{ label: 'send', type: 'success' }"
            :style="{width:'100%', margin: 'auto'}"
            @onAccept="notifyWorkers(experiment)"
            @onCancel="closeModal"
          > This email will be send to all workers that have finished a HIT for {{experiment.experimentName}}
            <BaseTextarea
              name="subject"
              :style="{marginBottom: '15px', marginTop: '50px', height: '40px'}"
              label="Please enter the subject of your email"
              :value="emailSubject"
              @keyPress="setEmailSubject"
            />
            <BaseTextarea
              name="message"
              :style="{marginBottom: '15px', marginTop: '50px'}"
              label="Please enter your message"
              :value="emailMessage"
              @keyPress="setEmailMessage"
            />
          </BaseModal>
        <tr :key="experiment._id">
          <td >
            <input :id="experiment._id" class="toggle" type="checkbox" @click="toggleActiveExperimentForExperimentMenu(experiment)"/>
            <label :for="experiment._id" class="lbl-toggle" style="text-align: left; font-size: 20px">{{experiment.experimentName}}</label>
            <div  v-if="activeExperimentIdForExperimentMenu==experiment._id" class="experimentMenuWrapper">
              <span @click="onExperimentEditClick(experiment)" :style="{textAlign: 'center', paddingTop: '5px', paddingBottom: '5px', borderBottom: '1px solid black'}" class="hoverable">EDIT</span>
              <span @click="onExperimentOverviewClick(experiment)" :style="{textAlign: 'center', paddingBottom: '5px', paddingTop: '5px'}" class="hoverable">OVERVIEW</span>
            </div>
          </td>
          <td>
            {{ experiment.description }}
          </td>
          <td class="align-center">{{ experiment.available }}</td>
          <td class="align-center">{{ experiment.pending }}</td>
          <td class="align-center">{{ experiment.waitingForApproval}}</td>
          <td class="align-center">{{ experiment.completed }}</td>
          <td class="button align-center">
            <BaseButton
                v-if="experiment.endpoint !== 'development'"
                prime
                square
                title="new hit (UTC)"
                fullWidth                
                @click="onNewHITClick(experiment)"
              
              >
              </BaseButton>
            <div class="scheduleWrapper" :style="{border: '1px solid black'}">              
              <Datetime v-if="showScheduleNewHIT==experiment._id" v-model="scheduledDateTime" readonly></Datetime>              
                <div v-if="showScheduleNewHIT==experiment._id" :style="{display: 'flex', flexDirection: 'column'}">
                  <div :style="{backgroundColor: 'white', padding: '10px 0', }">
                    <b>Los Angeles:</b> {{convertTZ('America/Los_Angeles')}}
                  </div>
                  <div :style="{backgroundColor: 'white', padding: '10px 0'}">
                    <b>New York:</b> {{convertTZ('America/New_York')}}
                  </div>
                </div >
                <div :style="{display: 'flex'}">
                  <BaseButton 
                    prime
                    red
                    square       
                    :style="{backgroundColor: '#ff5555', color: 'white', width: '50%', borderLeft: '0', borderBottom: '0'}"
                    title="close"
                    @click="onCloseNewHITClick()"
                    v-if="showScheduleNewHIT==experiment._id"
                  />
                  <BaseButton
                    prime
                    square
                    :style="{backgroundColor: '#00bfa5', color: 'white', width: '50%'}"
                    title="submit"
                    @click="onSubmitNewHitClick(experiment)"
                    v-if="showScheduleNewHIT==experiment._id"
                  />
                </div>
              </div>
            </td>
            <td class="button">
              <BaseButton
                v-if="experiment.endpoint !== 'development'"
                prime
                square              
                title="qualify all"
                fullWidth
                @click="onQualifyAllClick(experiment)"
              />
            </td>
            <td class="button">      
              <BaseButton
                    prime 
                    square
                    title="handle workers"                 
                    fullWidth
                    @click="toggleActiveExperimentForHandleWorkersMenu(experiment)"
                  />
              <div class="handleWorkersWrapper" v-if="handleWorkersVisible && activeExperimentIdForHandleWorkersMenu == experiment._id">
                <BaseButton
                  second
                  square                
                  title="notify workers"
                  fullWidth
                  white
                  noBorderBottom
                  @click="modalIsVisible=experiment._id"
                />
                <BaseButton
                  second
                  square
                  white
                  noBorderBottom           
                  title="approve workers"
                  @click="onApproveWorkersClick(experiment.rewardPerAssignment, experiment.awardQualificationID)"
                />
                <BaseButton
                  second
                  square
                  white
                  noBorderBottom
                  title="reject workers"
                  @click="onRejectWorkersClick()"
                />
                <BaseButton
                  second 
                  square
                  white                
                  title="close"
                  fullWidth
                  @click="closeHandleWorkersMenu(experiment)"
                />
            </div>
          </td>
        </tr>
        <br/>
        <template v-for="(hit, index) in experiment.hits">
          <tr :key="hit.HITId + 'header'">
            <td style="white-space: nowrap">
              <input :id="hit.HITId" class="toggle" type="checkbox" @click="toggleActiveHIT(hit.HITId)"/>
              <label :style="{whiteSpace: 'nowrap', fontSize: hit.HITId.includes('-') ? '10px' : '12px'}" :for="hit.HITId" class="lbl-toggle">{{ hit.HITId }}</label>
            </td>
            <td>
              Status: 
              <span :style="{color: hit.HITStatus == 'pending' ? 'orange' : hit.HITStatus == 'failed' ? 'red' : 'green', lineHeight: '20px' }">
                {{ hit.HITStatus }}
              </span> </br>
              <span style="white-space: nowrap; line-height: 20px" v-if="hit.HITStatus!= 'pending'">
                created at: {{parseCreationTime(hit.creationTime)}} </br>
                expires after: {{experiment.hitExpiresAfter}} days
              </span>         
            </td>
            <td v-if="hit.HITStatus=='pending' || hit.HITStatus=='failed'" colspan="4">scheduled for: {{hit.scheduledDateTime}}</td>
            <td v-if="hit.HITStatus!='pending' && hit.HITStatus!='failed'" class="align-center">{{ hit.available }}</td>
            <td v-if="hit.HITStatus!='pending' && hit.HITStatus!='failed'" class="align-center">{{ hit.pending }}</td>
            <td v-if="hit.HITStatus!='pending' && hit.HITStatus!='failed'" class="align-center">{{ hit.waitingForApproval }}</td>
            <td v-if="hit.HITStatus!='pending' && hit.HITStatus!='failed'" class="align-center">{{ hit.completed }}</td>
            <td class="button">
                <BaseButton :noBorderRight="hitStatus(hit)=='undefined'" prime square fullWidth @click="onHitClick(hit, experiment)">
                  Fullscreen
                </BaseButton>
            </td>
            <td v-if="hitStatus(hit)=='failed'" class="button">
              <BaseButton prime red square fullWidth @click="onCancelHitClick(hit)">
                Delete
              </BaseButton>
            </td>
            <td v-if="hitStatus(hit)=='cancelable'" class="button">
              <BaseButton prime red square fullWidth @click="onCancelHitClick(hit)">
                Cancel
              </BaseButton>
            </td>
            <td v-if="hitStatus(hit)=='expireable'" class="button">
              <BaseButton  prime orange square fullWidth @click="onExpireHitClick(experiment, hit)">
                Expire
              </BaseButton>
            </td>
            <td v-if="hitStatus(hit)=='deleteable'" class="button">
              <BaseButton  prime square red fullWidth @click="onDeleteHitClick(experiment, hit)">
                Delete
              </BaseButton>
            </td>
            <td v-if="hitStatus(hit)=='approvable'" class="button">
              <BaseButton prime grayLight square fullWidth @click="onNotifyApprovable()">
                Handle
              </BaseButton>
            </td>
          </tr>
          <tr v-if="activeHITId==hit.HITId" class="collapsible-content" :key="hit.HITId + 'workers'">
            <td colspan="100%" :style="{paddingLeft: '50px'}">
              <WorkersInline
                :HITId="hit.HITId"
                :awardid="experiment.awardQualificationId"
                :experimentID="experiment._id"
              />
            </td>
          </tr>

        </template>
      </template>
    </table>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

import BaseButton from '../BaseButton.vue'
import BaseCopy from '../BaseCopy.vue'
import BaseModal from '../BaseModal.vue'
import WorkersInline from '../../pages/WorkersInline.vue'
import { Experiment, Hit, Assignment } from '../../lib/types'
import api from '@/api'
//@ts-ignore
import Datetime from 'vuejs-datetimepicker';

export default Vue.extend({
  name: 'Tags',
  components: {
    BaseButton,
    BaseCopy,
    BaseModal,
    WorkersInline,
    Datetime
},
  props: {
    experiments: {
      type: Array,
      default: null,
    },
    parsedQualificationIDs: {
      type: String,
      default: ''
    }
  },
  data: () => ({
    activeExperimentIdForExperimentMenu: '',
    activeExperimentIdForHandleWorkersMenu: '',
    activeHITId: '',
    emailSubject: '',
    emailMessage: '',
    modalIsVisible: '',
    showScheduleNewHIT: '',
    showHandleWorkers: '',
    scheduledDateTime: '',
    handleWorkersVisible: false,
    showTimeInformation: false,
    activeExperiment: undefined,
    workerID: '',
    awardid: '',

  }),
  methods: {
    parseCreationTime(dateTime: string) {
      return new Date(dateTime).toLocaleDateString("en-US") + ' ' + new Date(dateTime).toLocaleTimeString("en-US")
    },
    toggleActiveExperimentForExperimentMenu(experiment: Experiment) {
      if(this.activeExperimentIdForExperimentMenu == experiment._id) this.activeExperimentIdForExperimentMenu = ""
      else this.activeExperimentIdForExperimentMenu = experiment._id
    },
    toggleActiveExperimentForHandleWorkersMenu(experiment: Experiment) {
      if (this.activeExperimentIdForHandleWorkersMenu == experiment._id) {
        this.activeExperimentIdForHandleWorkersMenu = ""
        this.handleWorkersVisible = false
      }
      else if (this.activeExperimentIdForHandleWorkersMenu == "") {
        this.activeExperimentIdForHandleWorkersMenu = experiment._id
        this.handleWorkersVisible = true
      }
      else {
        this.activeExperimentIdForHandleWorkersMenu = experiment._id
      }
    },
    closeHandleWorkersMenu(experiment: Experiment) {
      this.handleWorkersVisible = false
      this.activeExperimentIdForHandleWorkersMenu = ""
    },
    onExperimentEditClick(experiment: Experiment) {
      this.$router.push({
        name: 'Settings',
        query: { id: experiment._id },
        params: { experiment: experiment as any, initial: 'false', qualificationIDs: this.parsedQualificationIDs },
      })
    },
    onExperimentOverviewClick(experiment: Experiment) {
      const hitList = experiment.hits.map(hit => hit.HITId).toString()
      this.$router.push({
        name: 'Workers',
        query: {
          awardQualificationID: experiment.awardQualificationId,
          title: experiment.title,
          hitList,
          experimentId: experiment._id
        }
      })

    },
    onHitClick(hit: Hit, experiment: Experiment) {
      this.$router.push({
        name: 'Workers',
        params: {},
        query: {      
          HITId: hit.HITId,
          awardQualificationID: experiment.awardQualificationId,
        },
      })
    },
    onCancelHitClick(hit: Hit) {
      this.$emit('cancelHIT', hit)
    },
    onExpireHitClick(experiment: Experiment, hit: Hit) {
      this.$emit('expireHIT', experiment, hit)
    },
    onDeleteHitClick(experiment: Experiment, hit: Hit) {
      this.$emit('deleteHIT', experiment, hit)
    },
    onNewHITClick(experiment: Experiment) {
      if(this.showScheduleNewHIT==experiment._id) {
        this.showScheduleNewHIT = ""
        return 
      }     
      this.showScheduleNewHIT=experiment._id
      const date = new Date(new Date().getTime()  + (2 * 60 * 60 * 1000))
      this.scheduledDateTime= date.toISOString().slice(0,16).replace('T', ' ') + ' (now)'

    },
    onNotifyApprovable() {
      this.$toasted.show(
        "There is still assignments that need to be approved or rejected. Please handle those before further actions can be taken!", 
        {
          type: 'error',
          position: 'bottom-right',
          duration: 5000,
        })
    },
    onSubmitNewHitClick(experiment: Experiment) {
      this.$emit('createHIT', experiment, this.scheduledDateTime)
      this.showScheduleNewHIT = ''
      this.scheduledDateTime = ''
    },
    onApproveWorkersClick(rewardPerAssignment: string, awardQualificationID: string) {
      this.$emit('showAcceptAssignments', rewardPerAssignment, awardQualificationID)
    },
    onRejectWorkersClick() {
      this.$emit('showRejectAssignments')
    },
    async onQualifyAllClick(experiment: Experiment): Promise<void> {
      this.awardid = experiment.awardQualificationId
      const qualifiable = []
      for (const HIT of experiment.hits) {
        const res = await api.listAssignments({HITId: HIT.HITId})
        if (res.success){
          const assignments = res.data
          for (const assignment of assignments){
            const id = assignment.WorkerId
            console.log("assignment:",assignment)
            qualifiable.push(id)
        }
      }
      if (qualifiable != null) {
        console.log('Qualifying all Workers: ')
        for (const id of qualifiable) {
          console.log(
            'Asking for Qualifying Worker ' +
              id +
              ' with Qualification ' +
              this.awardid
          )
          this.qualifyWorker(id, true)
        }
      }
    }
    },
    async qualifyWorker(workerID: string, notify = false): Promise<void> {
      const awardid = this.awardid || ''
      console.log(
        'Qualifying Worker ' + workerID + ' with Qualification ' + awardid
      )

      const res = await api.qualifyWorker({
        awardQualificationID: awardid,
        workerID,
      })
      if (notify){
        if (res.success) {
          // await this.getWorkers()
          console.log('Qualified ' + workerID)
          this.$toasted.show(res.message, {
            type: 'success',
            position: 'bottom-right',
            duration: 3000,
          })
        } else {
          this.$toasted.show(res.message, {
            type: 'error',
            position: 'bottom-right',
            duration: 3000,
          })
        }
      }
    },
    hitStatus(hit: Hit) {
      const pending = parseInt(hit.pending.split('/')[0])
      const waitingForApproval = parseInt(hit.waitingForApproval.split('/')[0])
      if (hit.HITStatus == "failed") return 'failed'
      if (hit.HITStatus == "pending") return 'cancelable'
      if (hit.HITStatus == "Assignable") return 'expireable'
      if (waitingForApproval == 0 && pending == 0) return 'deleteable'     
      if (hit.HITStatus == "Reviewable" || hit.HITStatus == "Reviewing") return 'approvable'
      return 'unknown'
    },
    toggleActiveHIT(HITId: string) {
      if(this.activeHITId == HITId) this.activeHITId = ""
      else this.activeHITId = HITId
    },
    closeModal() {
        this.modalIsVisible = ''
    },
    setEmailSubject(val: any): void {
      this.emailSubject = val.subject
    },
    setEmailMessage(val: any): void {
      this.emailMessage = val.message
    },
    async notifyWorkers(experiment: Experiment) {
      
      const activeExperiment = (this.experiments as Experiment[]).filter((iterExperiment: Experiment) => iterExperiment._id == experiment._id
      )[0]
      const hitList = activeExperiment.hits
      let workerIDs: string[] = []
      for (const hit of hitList) {
        const assignmentRes = await api.listAssignments({ HITId: hit.HITId })
        if (assignmentRes.success) {
          assignmentRes.data.map((assignment: Assignment) => workerIDs.push(assignment.WorkerId))
        }     
      }
      this.closeModal()
      this.activeExperimentIdForHandleWorkersMenu = ""
      if(workerIDs) {
        console.log("workerIds:", workerIDs.flat())
        const notificationRes = await api.notifyWorkers({subject: this.emailSubject, message: this.emailMessage, workerIDs: workerIDs.flat()})
        if(notificationRes.success) {
          this.$toasted.success(notificationRes.message, {
            position: 'bottom-right',
            duration: 3000,
          })
        }
        else {
          this.$toasted.error(notificationRes.message, {
            position: 'bottom-right',
            duration: 3000,
          })
        }
      }
    },
    onScheduleNewHitClick(experiment: Experiment) {
      this.showScheduleNewHIT = experiment._id
    },
    onCloseNewHITClick() {
      this.showScheduleNewHIT = ''
      this.scheduledDateTime = ''
    },
    convertTZ(tzString: string) {
      const scheduledFor = this.scheduledDateTime.replace(' (now)', '')
      return (scheduledFor != "" ? new Date(scheduledFor) : new Date()).toLocaleString("en-US", {timeZone: tzString});   
    },
  }
})
</script>
<style lang="scss">
  table {
    width: 100%;
    border-spacing: 0 1em;
  }
  tr {
    width: 100%;
    margin-top: 10px;
  }
  td {
    padding: 0 10px;
    font-size: 12px;
  }
  td.button {
    padding: 0 10px 0 0
  }

.Overview input[type='checkbox'] {
  display: none;
}
.lbl-toggle {
  display: block;

  font-weight: bold;
  font-family: monospace;
  text-transform: uppercase;
  text-align: center;

  padding: 8px;

  color: #a77b0e;

  cursor: pointer;

  transition: all 0.25s ease-out;

  flex: 1.5;
}

.lbl-toggle:hover {
  color: #7c5a0b;
}

.lbl-toggle::before {
  content: ' ';
  display: inline-block;

  border-top: 5px solid transparent;
  border-bottom: 5px solid transparent;
  border-left: 5px solid currentColor;

  vertical-align: middle;
  margin-right: 0.7rem;
  transform: translateY(-2px);

  transition: transform 0.2s ease-out;
}

.toggle:checked ~ .lbl-toggle::before {
  //border-right: 5px solid transparent;
  //border-top: 5px solid currentColor;
  //border-left: 5px solid transparent;
  transform: rotate(90deg);
}

.collapsible-content {
  overflow: scroll;
  transition: max-height 0.25s ease-in-out;
  max-height: 100vh;
}

.hoverable:hover{
  background-color: rgba(color(gray-dark), 0.25);
}

.buttons {
  width: 40%;
  display: flex;
}
.no-wrap {
  white-space: nowrap
}
.scheduleWrapper, .handleWorkersWrapper {
  display: flex;
  position: absolute; 
  flex-direction: column;
  z-index: 1;
  margin: 10px;
}


.year-month-wrapper {
  background-color: white !important;
}

.month, .year {
  color: black !important
}

.nav-r, .nav-l, .activePort, .okButton{
  background-color: black !important
}

.days, .okButton {
  color: white !important;
}

.experimentMenuWrapper {
  display: flex;
  flex-direction: column;
  border: 1px solid black;
  margin-top: 10px;
  position: absolute;
  background-color: white;
  z-index: 1;
  box-shadow: 0px 2px 20px 0 rgba(lighten(color(gray-dark), 10%), 0.4);
  font-weight: bold;
  > span {
    padding: 0 20px;
  }
}
.dateTimeInformation {
  position: absolute;
  top: 5px;
  right: 5px;
}

.align-center {
  text-align: center
}
</style>
