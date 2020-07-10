<template>
    <div class="Overview">
        <BaseHeadline
                :route="backbutton"
                prime
                title="myTurk"
                description="Overview of your experiments"
        />

        <BaseWrapper title="Production" red>
            <Table
                    :experiments="experiments.production"
                    @createHIT="createHIT"
                    @onHitDeleteClick="handleDeleteHIT"
            />
        </BaseWrapper>

        <BaseWrapper title="Sandbox" green>
            <Table
                    :experiments="experiments.sandbox"
                    @createHIT="createHIT"
                    @onHitDeleteClick="handleDeleteHIT"
            />
        </BaseWrapper>
        <BaseButton prime title="new experiment" @click="addExperiment"/>

        <BaseModal
                :visible="modalIsVisible"
                title="Delete HIT"
                :cancel="{ label: 'cancel' }"
                :accept="{ label: 'delete', type: 'warning' }"
                @onAccept="deleteHIT"
                @onCancel="closeModal"
        >
            <p>Are you sure you want to delete this HIT?</p>
        </BaseModal>
    </div>
</template>
<script>
    import BaseButton from '@/components/BaseButton.vue'
    import BaseHeadline from '@/components/BaseHeadline.vue'
    import BaseModal from '@/components/BaseModal.vue'
    import BaseWrapper from '@/components/BaseWrapper.vue'
    import Table from '@/components/overview/Table.vue'
    import api from '@/api'
    import utils from '@/utils'

    export default {
        name: 'Tags',
        components: {
            BaseHeadline,
            BaseModal,
            BaseWrapper,
            BaseButton,
            Table,
        },
        data: () => ({
            backbutton: {
                paths: 'login',
                name: 'Click here to SignOut',
                params: {loggedOut: true},
                hit: {},
            },
            modalIsVisible: false,
            experiments: {},
        }),
        mounted: async function () {
            this.getExperiments()
        },
        methods: {
            async getExperiments() {
                try {
                    let result = await api.getExperiments({groupBy: 'endpoint'})
                    this.experiments = result.data
                    this.experiments.production = result.data.production || []
                    this.experiments.sandbox = result.data.sandbox || []
                } catch (errorResponse) {
                    let errorMessage = await utils.resolveErrorMessage(errorResponse);

                    this.$toasted.error(errorMessage, {
                        position: 'bottom-right',
                        duration: 3000,
                    })
                }
            },
            async addExperiment() {
                this.$router.push({
                    name: 'settings',
                    params: {addExperiment: true},
                })
            },
            async createHIT(experiment) {
                console.log(experiment)
                try {
                    let res = await api.createHIT(experiment.id)
                    console.log(res)
                    this.$toasted.success(res.message, {
                        position: 'bottom-right',
                        duration: 3000,
                    })
                    let hit = res.data
                    experiment = this.addHITtoExperiment(experiment, hit)

                    let id = experiment.id
                    await api.saveExperiment({id, experiment})
                    this.getExperiments()
                } catch (errorResponse) {
                    let errorMessage = await utils.resolveErrorMessage(errorResponse);

                    this.$toasted.error(errorMessage, {
                        position: 'bottom-right',
                        duration: 3000,
                    })
                }
            },
            addHITtoExperiment(experiment, hit) {
                let hitID = hit.HitId
                let maxAssignments = hit.MaxAssignments
                let available = hit.NumberOfAssignmentsAvailable
                let pending = hit.NumberOfAssignmentsPending
                let completed = hit.NumberOfAssignmentsCompleted
                let creationTime = hit.CreationTime
                let title = hit.Title
                let status = hit.HITStatus
                let waitingForApproval = maxAssignments - available - completed - pending

                //TODO missmatch between server:localHit and this thing here:
                //available: 2 vs available "2/2"
                let mHIT = {
                    id: hitID,
                    title: title,
                    available: `${available} / ${maxAssignments} `,
                    pending: `${pending} / ${maxAssignments} `,
                    waitingForApproval: `${waitingForApproval} / ${maxAssignments} `,
                    completed: `${completed} / ${maxAssignments} `,
                    creationTime: creationTime,
                    status: status,
                }

                if (!experiment.localHits) {
                    experiment.localHits = []
                }
                experiment.localHits.push(mHIT)
                return experiment
            },
            handleDeleteHIT(hit) {
                this.modalIsVisible = true
                this.hit = hit
            },
            async deleteHIT() {
                let id = this.hit.id
                try {
                    let res = await api.deleteHIT({id})
                    this.getExperiments()

                    this.$toasted.success(res.message, {
                        position: 'bottom-right',
                        duration: 3000,
                    })
                } catch (errorResponse) {
                    let errorMessage = await utils.resolveErrorMessage(errorResponse);

                    this.$toasted.error(errorMessage, {
                        position: 'bottom-right',
                        duration: 3000,
                    })
                } finally {
                    this.modalIsVisible = false
                    this.hit = {}
                }

            },
            closeModal() {
                this.modalIsVisible = false
            },
        },
    }
</script>
<style lang="scss">
    .Overview {
        position: relative;

        > .BaseButton.is-prime {
            position: absolute;
            top: 0;
            right: 0;
        }
    }
</style>
