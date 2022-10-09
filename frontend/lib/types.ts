export type Credentials = {
  awsAccessKeyId: string,
  awsSecretAccessKey: string,
  endpoint: string
}

export type Payload = Record<string, any>

export type APIRes = {
  success: boolean,
  token?: string,
  message: string,
  endpoint?: string,
  data: any
}

export type Experiment = {
  _id: string
  experimentName: string,
  title: string,
  description: string,
  entrypoint: string,
  available: string,
  pending: string,
  waitingForApproval: string,
  completed: string,
  keywords: string,
  awardQualificationName: string,
  awardQualificationDescription: string,
  awardQualificationId: string,
  hitExpiresAfter: string,
  assignmentDurationInMinutes: string,
  assignmentsPerHit: string,
  defaultRequirements: boolean,
  guardHitByQualification: boolean,
  rewardPerAssignment: string,
  hits: Hit[],
  endpoint: string,
  guardHitByAdditionalQualificationids: string[] | string
  excludeWorkersByQualificationid: string[] | string,
  automaticalyAssignQualification: boolean,
  automaticalyEndHITs: boolean
  
}

export type Hit = {
  HITId: string,
  available: string,
  pending: string,
  waitingForApproval: string,
  completed: string,
  maxAssignments: string,
  creationTime: number,
  title: string,
  HITStatus: string,
  numberOfAssignmentsAvailable?: number
  scheduledDateTime?: string
  timeoutId?: string
}

export type BaseText = {
    label: string,
    type: string,
    value: string,
}

export type WorkersData = {
  isExperimentView: boolean | undefined
  experimentId: string,
  HITId: string,
  title: string,
  creationTime: string,
  awardQualificationID: string,
  modalRejectIsVisible: boolean,
  modalApproveIsVisible: boolean,
  rejectFeedback:
    string,
  approvalFeedback: string,
  assignmentID: string,
  workerID: string,
  route: {
    path: string,
    name: string,
  },
  submitted: any[] | null,
  approved: any[] | null,
  rejected: any[] | null,
  feedback?: string,
  approveMessages: string[],
  rejectMessages: string[],
  saveMessage: boolean | undefined
  awardQualificationId?: string
}
  
export type Worker = {
  id: string,
  HITId: string,
  assignmentID: string
  started: {
    time: string,
    date: string,
  },
  finished: {
    time: string,
    date: string,
  },
  rejected?: {
    time: string,
    date: string,
  },
  approved?: {
    time: string,
    date: string,
  },
  awardQualificationId?: string
}

export type Route = {
  path: string
  name: string
  params: Record<string, any>
}

export type Message = {
  id: string
  type: string
  message: string
}

export type Assignment = {
  AssignmentId: string,
  WorkerId: string,
  HITId: string
}

export type SettingsGroup = {
  title: string,
  items: {
    name: string,
    value: string | boolean,
    info: string,
    disabled?: boolean,
    hint?: string,
    type?: string,
    isQualificationId?: boolean
  }[],
}

