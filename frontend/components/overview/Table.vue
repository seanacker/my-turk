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
      <template v-for="(experiment, index) in experiments" bold>
        <BaseModal
            style="position: fixed; top: 0; left: 0"
            :visible="modalIsVisible==experiment._id"
            v-if="true"
            :key="experiment._id + 'Emailmodal'"
            title="Send Email"
            :cancel="{ label: 'cancel' }"
            :accept="{ label: 'send', type: 'success' }"
            :style="{width:'100%', margin: 'auto'}"
            @onAccept="notifyWorkers(experiment)"
            @onCancel="closeModal"
          > This email will be send to all approved workers from experiment <b>{{experiment.experimentName}}</b>
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
        <tr 
          :key="experiment._id" 
          v-if="true"
          style="height: 75px; background-color: beige; "
          >
          <td v-bind:id="'title' + experiment._id">
            <span style="font-weight: bold; font-size: 16px; cursor: pointer;" @click="toggleActiveExperiment(experiment)"> 
              <fa   style="cursor: pointer;" class="fa-lg" :icon="activeExperimentId==experiment._id || activeExperimentId == 'true'? 'angle-down' : 'angle-right' " ></fa>&nbsp;&nbsp;{{experiment.experimentName}}
            </span>       
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
                second
                white
                square
                title="new hit"
                fullWidth                
                @click="onNewHITClick(experiment)"
              
              >
              </BaseButton>
            <div class="scheduleWrapper" :style="{border: '1px solid black'}" :id="'schedule' + experiment._id">              
              <Datetime style="cursor: pointer;" v-if="showScheduleNewHIT==experiment._id" v-model="scheduledDateTime" readonly></Datetime>              
                <div v-if="showScheduleNewHIT==experiment._id" :style="{display: 'flex', flexDirection: 'column', textAlign: 'center'}">
                  <i :style="{backgroundColor: 'white', padding: '10px 0', }">
                   This is in Eastern Daylight Time, <br/>
                   the standard timezone of MTurk
                  </i>
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
                @click="onExperimentOverviewClick(experiment)" 
                second
                white
                square
                title="overview"
                fullWidth />
            </td>
            <td class="button">
              <BaseButton 
                @click="onExperimentEditClick(experiment)" 
                second
                white
                square
                title="edit"
                fullWidth  
              />
            </td>
            <td class="button" v-click-outside="closeHandleWorkersMenu">      
              <BaseButton
              second
                white
                    square
                    title="handle workers"                 
                    fullWidth
                    @click="toggleActiveExperimentForHandleWorkersMenu(experiment)"
                  />
              <div class="handleWorkersWrapper" v-if="handleWorkersVisible && (activeExperimentIdForHandleWorkersMenu == experiment._id || activeExperimentId == 'true')">
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
                  @click="onApproveWorkersClick(experiment.rewardPerAssignment, experiment.awardQualificationId)"
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
                  v-if="experiment.endpoint !== 'development'"
                  second
                  square
                  white         
                  title="qualify all"
                  fullWidth
                  @click="onQualifyAllClick(experiment)"
                />
            </div>
          </td>
        </tr>
        <template v-for="(hit, index) in experiment.hits" >
          <tr :key="hit.HITId + 'body'" v-if="activeExperimentId==experiment._id || activeExperimentId == 'true'" >
            <td style="white-space: nowrap" @click="toggleActiveHIT(hit.HITId)">
              <label 
              :style="{whiteSpace: 'nowrap', fontSize: hit.HITId.includes('-') ? '10px' : '12px', marginTop: index == 0 ? '40px' : '', paddingLeft: '20px'}" 
              :for="hit.HITId" class="lbl-toggle">
                <fa :icon="activeHITId==hit.HITId ? 'angle-down' : 'angle-right'"></fa>
                {{ hit.HITId }}
              </label>
            </td>
            <td>
              <table class="hitDetails">
                <tr v-if="index==0" style="height: 24">
                  <td style="width: 25%" >
                    <a href="https://blog.mturk.com/understanding-hit-states-d0bc9806c0ee" target="_blank">Status</a>
                  </td>
                  <td style="width: 25%">
                    Creation Date (EDT)
                  </td>
                  <td style="width: 25%">
                    Creation Time (EDT)
                  </td>
                  <td style="width: 25%">
                    Expires after (days)
                  </td>
                </tr>
                <tr>
                  <td>
                    <span :style="{color: hit.HITStatus == 'pending' ? 'orange' : hit.HITStatus == 'failed' ? 'red' : 'green', lineHeight: '20px', width: '25%' }">
                      {{ hit.HITStatus }}
                   </span>
                  </td>
                  <td style="width: 25%">
                    {{ hit.HITStatus == 'pending' ? parseScheduledDate(hit) : parseCreationDate(hit.creationTime) }}
                  </td>
                  <td style="width: 25%">
                    {{ hit.HITStatus == 'pending' ? parseScheduledTime(hit) : parseCreationTime(hit.creationTime) }}
                  </td>
                  <td style="width: 25%">
                    {{experiment.hitExpiresAfter}}
                  </td>
                </tr>
              </table>      
            </td>
            <td v-if="hit.HITStatus=='pending' || hit.HITStatus == 'failed'" colspan="4" style="text-align: left; padding-left: 25px;"><p :style="{marginTop: index == 0 ? '40px' : ''}">{{hit.available.slice(0,5)}}</p></td>
            <td v-if="hit.HITStatus!='pending' && hit.HITStatus!='failed'" class="align-center" ><p :style="{marginTop: index == 0 ? '42px' : ''}">{{ hit.available }}</p></td>
            <td v-if="hit.HITStatus!='pending' && hit.HITStatus!='failed'" class="align-center" ><p :style="{marginTop: index == 0 ? '42px' : ''}">{{ hit.pending }}</p></td>
            <td v-if="hit.HITStatus!='pending' && hit.HITStatus!='failed'" class="align-center" ><p :style="{marginTop: index == 0 ? '42px' : ''}">{{ hit.waitingForApproval }}</p></td>
            <td v-if="hit.HITStatus!='pending' && hit.HITStatus!='failed'" class="align-center" ><p :style="{marginTop: index == 0 ? '42px' : ''}">{{ hit.completed }}</p></td>
            <td class="button">
                <BaseButton :style="{marginTop: index == 0 ? '40px' : ''}" :noBorderRight="hitStatus(hit)=='unknown'" prime square fullWidth @click="onHitClick(hit, experiment)">
                  Fullscreen
                </BaseButton>
            </td>
            <td v-if="hitStatus(hit)=='failed'" class="button">
              <BaseButton :style="{marginTop: index == 0 ? '40px' : ''}" prime red square fullWidth @click="onCancelHitClick(hit)">
                Delete
              </BaseButton>
            </td>
            <td v-if="hitStatus(hit)=='cancelable'" class="button">
              <BaseButton :style="{marginTop: index == 0 ? '40px' : ''}" prime red square fullWidth @click="onCancelHitClick(hit)">
                Cancel
              </BaseButton>
            </td>
            <td v-if="hitStatus(hit)=='expireable'" class="button">
              <BaseButton  :style="{marginTop: index == 0 ? '40px' : ''}" prime orange square fullWidth @click="onExpireHitClick(experiment, hit)">
                End
              </BaseButton>
            </td>
            <td v-if="hitStatus(hit)=='deleteable'" class="button">
              <BaseButton  :style="{marginTop: index == 0 ? '40px' : ''}" prime square red fullWidth @click="onDeleteHitClick(experiment, hit)">
                Delete
              </BaseButton>
            </td>
            <td v-if="hitStatus(hit)=='approvable'" class="button" @click="onNotifyApprovable()">
              <BaseButton disabled :style="{marginTop: index == 0 ? '40px' : ''}" prime grayLight square fullWidth >
                Delete
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
        <tr column-span="all" v-if="(index+1)<experiments.length" :key="'firstMargin' + index">
          <td style="height: 40px; font-size: 2px " colspan="9">&nbsp;</td>
        </tr>
        <tr class="hr" column-span="all" v-if="(index+1)<experiments.length" :key="'hr' + index">
          <td style="height: 1px; font-size: 2px " colspan="9">&nbsp;</td>
        </tr>
        <tr  column-span="all" v-if="(index+1)<experiments.length" :key="'secondMargin' + index">
          <td style="height: 40px; font-size: 2px " colspan="9">&nbsp;</td>
        </tr>
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
//@ts-ignore
import vClickOutside from 'v-click-outside'

type TableData = {
  activeExperimentId: string,
  activeExperimentIdForHandleWorkersMenu: string,
  activeHITId: string,
  emailSubject: string,
  emailMessage: string,
  modalIsVisible: string,
  showScheduleNewHIT: string,
  showHandleWorkers: string,
  scheduledDateTime: string,
  handleWorkersVisible: boolean,
  showTimeInformation: boolean,
  activeExperiment: Experiment | undefined,
  workerID: string,
  awardid: string,
  showStatusInfo: string
}

export default Vue.extend({
  name: 'Tags',
  components: {
    BaseButton,
    BaseCopy,
    BaseModal,
    WorkersInline,
    Datetime
  },
  directives: {
    clickOutside: vClickOutside.directive
  },
  props: {
    experiments: {
      type: Array as () => Experiment[],
      default: null,
    },
    parsedQualificationIDs: {
      type: String,
      default: ''
    },
    refreshIntervalId: {
      type: Number,
      default: 0
    }
  },
  data: () :TableData => ({
    activeExperimentId: 'true',
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
    showStatusInfo: ''
  }),
  methods: {
    //Experiment Methods

    // UI
    toggleActiveExperiment(experiment: Experiment) {
      if(this.activeExperimentId == experiment._id || this.activeExperimentId == 'true') this.activeExperimentId = ''
      else this.activeExperimentId = experiment._id
    },
    closeExperimentMenu() {
      this.activeExperimentId = ""
    },
    onExperimentEditClick(experiment: Experiment) {
      clearInterval(this.refreshIntervalId)
      this.$router.push({
        name: 'Settings',
        query: { id: experiment._id },
        params: { experiment: experiment as any, initial: 'false', qualificationIDs: this.parsedQualificationIDs },
      })
    },
    onExperimentOverviewClick(experiment: Experiment) {
      clearInterval(this.refreshIntervalId)
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
    toggleActiveExperimentForHandleWorkersMenu(experiment: Experiment) {
      setTimeout(() => {
        this.handleWorkersVisible = true
        this.activeExperimentIdForHandleWorkersMenu = experiment._id
      }, 200)
    },
    closeHandleWorkersMenu() {
      this.handleWorkersVisible = false
      this.activeExperimentIdForHandleWorkersMenu = ""
    },
    closeModal() {
        this.modalIsVisible = ''
    },
    onScheduleNewHitClick(experiment: Experiment) {
      this.showScheduleNewHIT = experiment._id
    },
    onCloseNewHITClick() {
      this.showScheduleNewHIT = ''
      this.scheduledDateTime = ''
    },

    // New HIT and HIT scheduling
    convertTZ(tzString: string) {
      const scheduledFor = this.scheduledDateTime.replace(' (now)', '')
      return (scheduledFor != "" ? new Date(scheduledFor) : new Date()).toLocaleString("en-US", {timeZone: tzString});   
    },

    onNewHITClick(experiment: Experiment) {
      if(this.showScheduleNewHIT==experiment._id) {
        this.showScheduleNewHIT = ""
        return 
      }     
      this.showScheduleNewHIT=experiment._id
      const date = new Date(new Date().getTime()  - (5 * 60 * 60 * 1000))
      this.scheduledDateTime= date.toISOString().slice(0,16).replace('T', ' ') 

    },
    onSubmitNewHitClick(experiment: Experiment) {
      this.$emit('createHIT', experiment, this.scheduledDateTime)
      this.showScheduleNewHIT = ''
      this.scheduledDateTime = ''
    },


    
    // Notifying Workers
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
          assignmentRes.data.map((assignment: Assignment) => {
          if (assignment.AssignmentStatus == "Approved") workerIDs.push(assignment.WorkerId)
        })
        }     
      }
      this.closeModal()
      this.activeExperimentIdForHandleWorkersMenu = ""
      if(workerIDs) {
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



    // HIT Methods
    onHitClick(hit: Hit, experiment: Experiment) {
      clearInterval(this.refreshIntervalId)
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

    onNotifyApprovable() {
      this.$toasted.show(
        "There is still assignments that need to be approved or rejected. Please handle those before further actions can be taken!", 
        {
          type: 'error',
          position: 'bottom-right',
          duration: 5000,
        })
    },

    onApproveWorkersClick(rewardPerAssignment: string, awardQualificationID: string) {
      this.$emit('showAcceptAssignments', rewardPerAssignment, awardQualificationID)
    },
    onRejectWorkersClick() {
      this.$emit('showRejectAssignments')
    },
    async  onQualifyAllClick(experiment: Experiment): Promise<void> {
      this.awardid = experiment.awardQualificationId
      const qualifiable = []
      for (const HIT of experiment.hits) {
        const res = await api.listAssignments({HITId: HIT.HITId})
        if (res.success){
          const assignments = res.data
          for (const assignment of assignments){
            const id = assignment.WorkerId
            qualifiable.push(id)
        }
      }
      if (qualifiable != null) {
        for (const id of qualifiable) {
          this.qualifyWorker(id, true)
        }
      }
    }
    },
    async qualifyWorker(workerID: string, notify = false): Promise<void> {
      const awardid = this.awardid || ''

      const res = await api.qualifyWorker({
        awardQualificationID: awardid,
        workerID,
      })
      if (notify){
        if (res.success) {
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
    parseCreationTime(dateTime: number) {
      const utcTime = new Date(dateTime)
      const utcOffsetSinceZero = utcTime.getTime()
      const usEastOneOffsetSinceZero = utcOffsetSinceZero - 7 * 60 * 60 * 1000 
      return new Date(usEastOneOffsetSinceZero).toLocaleTimeString("en-US")
    },
    parseCreationDate(dateTime: number) {
      const utcTime = new Date(dateTime)
      const utcOffsetSinceZero = utcTime.getTime()
      const usEastOneOffsetSinceZero = utcOffsetSinceZero - 7 * 60 * 60 * 1000 
      return new Date(usEastOneOffsetSinceZero).toLocaleDateString("en-US")
    },
    parseScheduledDate(hit: Hit) {
      return new Date(hit.scheduledDateTime as string).toLocaleDateString("en-US")
    },
    parseScheduledTime(hit: Hit){
      return new Date(hit.scheduledDateTime as string).toLocaleTimeString("en-US")
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
      if (this.activeHITId == HITId) this.activeHITId = ''
      else this.activeHITId = HITId 
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
    &.hr {
      border: 0;
      height: 1px; 
      background-image: linear-gradient(to right, rgba(0, 0, 0, 0), rgba(0, 0, 0, 0.75), rgba(0, 0, 0, 0));
    }
  }
  td {
    padding: 0 10px;
    font-size: 12px;
  }
  td.button {
    padding: 0 10px 0 0
  }
table.hitDetails > tr {
  text-align: center;
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
#tj-datetime-input{
  cursor: pointer;
  text-align: center
}
</style>
