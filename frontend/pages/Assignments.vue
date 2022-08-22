<template>
    <div class="Assignments">
        <BaseHeadline
            :route="route"
            title="Approve or Reject Assignments"
            :description="`You are in ${endpoint} mode`"
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
export default Vue.extend({
    name: 'Tags',
    components: {
        BaseButton,
        BaseHeadline,
        BaseModal
    },
    data: () => ({
        endpoint: ''
    })
})
</script>