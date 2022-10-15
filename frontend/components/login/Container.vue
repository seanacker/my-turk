<template>
  <BaseWrapper title="" green>
    <div class="Container">
      <span class="Hint">
        <fa icon="circle-info"/>
        <span
          >Your credentials might be under ./username/.aws/<br />
          Otherwise create new credentials
          <a
            href="https://docs.aws.amazon.com/de_de/sdk-for-net/v2/developer-guide/net-dg-setup.html#net-dg-signup"
            target="_blank"
            >here</a
          >
        </span>
      </span>
      <BaseInput
        :label="items[0]"
        value=""
        type="text"
        @keyPress="handleKeyPress"
      />
      <BaseInput
        :label="items[1]"
        value=""
        type="password"
        @keyPress="handleKeyPress"
      />
      <BaseSelect
        :options="options"
        label="Endpoint"
        @onChange="handleSelectChange"
      />
    </div>
    <BaseButton square prime title="sign in" @click="handleLogin" />
  </BaseWrapper>
</template>

<script lang="ts">
import Vue from 'vue'

import BaseInput from '../BaseInput.vue'
import BaseButton from '../BaseButton.vue'
import BaseWrapper from '../BaseWrapper.vue'
import BaseSelect from '../../components/BaseSelect.vue'
import { Credentials } from '../../lib/types'

type LoginContainerData = {
  awsAccessKeyId: string,
  awsSecretAccessKey: string,
  endpoint: string
    options: [
      {
        value: string,
        label: string,
        isSelected: boolean,
      },
      {
        value: string,
        label: string,
        isSelected: boolean,
      },
    ],
}

export default Vue.extend({
  name: 'Grid',
  components: {
    BaseInput,
    BaseButton,
    BaseWrapper,
    BaseSelect,
  },
  props: {
    items: {
      type: Array,
      default: () => [],
    },
  },
  data: (): LoginContainerData => ({
    awsAccessKeyId: '',
    awsSecretAccessKey: '',
    endpoint: '',
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
  methods: {
    handleKeyPress({ awsAccessKeyId, awsSecretAccessKey }: Partial<Credentials>) {
      this.awsAccessKeyId = awsAccessKeyId || this.awsAccessKeyId
      this.awsSecretAccessKey = awsSecretAccessKey || this.awsSecretAccessKey
    },
    handleLogin() {
      this.endpoint =
        this.endpoint ||
        'https://mturk-requester-sandbox.us-east-1.amazonaws.com'
      const { awsAccessKeyId, awsSecretAccessKey, endpoint } = this
      this.$emit('login', { awsAccessKeyId, awsSecretAccessKey, endpoint })
    },
    handleSelectChange(val: { endpoint: string }) {
      this.endpoint = val.endpoint
    },
  },
})
</script>
<style scoped lang="scss">
.BaseWrapper {
  .Container {
    position: relative;
    transform: translateX(-20px);
    width: calc(100% + 20px);
    padding: 30px 0 20px;
    display: grid;
    grid-gap: 30px 0;
  }

  .BaseButton {
    align-self: baseline;
    margin-left: -20px;
    margin-bottom: 10px;
  }

  .Hint {
    color: color(text-light);
    transform: translate(15px, -10px);
    font-size: rem(16px);
    display: flex;
    align-items: center;

    a,
    span {
      font-size: rem(12px);
      line-height: 1.5;
    }

    span {
      padding-left: 10px;
    }
  }
}
</style>
