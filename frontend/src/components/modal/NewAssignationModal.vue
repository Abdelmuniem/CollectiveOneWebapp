<template lang="html">
  <div class="w3-modal">
    <div class="w3-modal-content">
      <div class="w3-card-4 app-modal-card">
        <div class="close-div w3-display-topright w3-xlarge" @click="closeThis()">
          <i class="fa fa-times" aria-hidden="true"></i>
        </div>

        <div class="w3-container w3-border-bottom">
          <transition name="fadeenter" mode="out-in">
            <h2 :key="isPeerReviewed">
              {{ isPeerReviewed ? $t('tokens.PEER_REVIEW_TITLE') : $t('tokens.DIRECT_TITLE') }}
            </h2>
          </transition>
        </div>

        <div class="this-container w3-container">

          <div class="section-tabs w3-row">
            <div
              id="T_directTransferUser"
              class="w3-col s6 tablink w3-bottombar w3-hover-light-grey w3-padding"
              :class="{'border-blue-app': isDirect}"
              @click="assignation.type = DIRECT_ID()">
              <h5 class="" :class="{'bold-text': isDirect}">{{ $t('tokens.DIRECT') }}</h5>
            </div>
            <div
              id="T_peerReviewedTransferUser"
              class="w3-col s6 tablink w3-bottombar w3-hover-light-grey w3-padding"
              :class="{'border-blue-app': isPeerReviewed}"
              @click="assignation.type = PEER_REVIEWED_ID()">
              <h5 class="" :class="{'bold-text': isPeerReviewed}">{{ $t('tokens.PEER_REVIEWED') }}</h5>
            </div>
          </div>
          <br>

          <div class="slider-container">
            <transition name="slideDownUp" mode="out-in">
              <keep-alive>
                <component @updated="receiversUpdated($event)" :is="isDirect ? 'app-direct-assignation' : 'app-peer-reviewed-assignation'"></component>
              </keep-alive>
            </transition>
            <app-error-panel
              :show="notEnoughReceivers"
              :message="$t('toknes.NOT_ENOUGH_RECEIVERS')">
            </app-error-panel>
            <app-error-panel
              :show="notEnoughEvaluators"
              :message="$t('toknes.NOT_ENOUGH_EVALS')">
            </app-error-panel>
            <app-error-panel
              :show="allDonorsShow"
              :message="$t('toknes.NOT_ALL_DONORS')">
            </app-error-panel>
            <app-error-panel
              :show="percentagesWrongShow"
              :message="$t('toknes.PERCENTAGES_WRONG')">
            </app-error-panel>
          </div>

          <div class="w3-row">
            <app-initiative-assets-assigner
              :initiativeId="initiative.id"
              :initiativeName="initiative.meta.name"
              type='member-assigner'
              :assetId="assetId"
              :showSelector="true"
              @updated="assetsSelected($event)" :showError="assetsZeroShow">
            </app-initiative-assets-assigner>
            <app-error-panel
              :show="assetsZeroShow"
              :message="$t('tokens.NO_ASSETS_SELECTED')">
            </app-error-panel>
          </div>

          <div class="w3-row">
            <label class=""><b>{{ $t('tokens.MOTIVE') }} <span class="w3-small error-text">({{ $t('general.REQUIRED') }})</span></b></label>
            <input v-model="assignation.motive" class="w3-input w3-hover-light-grey" :class="{ 'error-input' : motiveErrorShow }" type="text">
            <app-error-panel
              :show="motiveEmptyShow"
              :message="$t('general.FIELD_CANNOT_BE_EMPTY')">
            </app-error-panel>
            <app-error-panel
              :show="motiveTooLarge"
              :message="$t('general.FIELD_TOO_LONG')">
            </app-error-panel>
            <br>

            <label class=""><b>{{ $t('general.NOTES') }}</b></label>
            <app-markdown-editor
              v-model="assignation.notes"
              :keepBackup="false"
              :showBorder="true">
            </app-markdown-editor>
            <br>
          </div>

          <div class="slider-container">
            <transition name="slideDownUp">
              <div v-if="isPeerReviewed" class="w3-row">
                <div class="w3-col s12">
                  <div class="w3-row">
                    <label class=""><b>{{ $t('general.CONFIGURATION') }}</b>
                      <popper
                        :key="$store.state.support.isTouchScreen + Math.random()*100000000"
                        :trigger="$store.state.support.isTouchScreen ? 'long-press' : 'hover'"
                        :options="popperOptions" :delay-on-mouse-in="600" :delay-on-mouse-out="800">
                        <app-help-popper
                          :title="$t('help.PEER-REV-CONFIG-TT')"
                          :details="$t('help.PEER-REV-CONFIG-DET')">
                        </app-help-popper>

                        <div slot="reference" @click="$emit('docView')" class="help-icon">
                          <img src="./../../assets/question-icon.svg" alt="">
                        </div>
                      </popper>
                    </label>
                  </div>
                  <div class="w3-row-padding configuration-row">
                    <div class="w3-col m6">
                      <div class="w3-row">
                        <div class="w3-col l3 m12">
                          <label class="w3-left first-label">{{ $t('tokens.INITIAL_STATE') }}:</label>
                        </div>
                        <div class="w3-col l9 m12 w3-center state-btns">
                          <button class="w3-button"
                            :class="{'app-button': isStartOnHold, 'app-button-light': !isStartOnHold}" name="button"
                            @click="assignation.config.startState = 'ON_HOLD'">
                            {{ $t('tokens.ON_HOLD') }}
                          </button>
                          <button class="w3-button"
                            :class="{'app-button': isStartOpen, 'app-button-light': !isStartOpen}" name="button"
                            @click="assignation.config.startState = 'OPEN'">
                            {{ $t('tokens.OPEN') }}
                          </button>
                        </div>
                      </div>
                      <div class="w3-row">
                        <label class="w3-left first-label">{{ $t('tokens.MAX_DURATION') }}</label>
                        <input v-model="assignation.config.maxDuration" class="w3-input w3-left input-number" type="number">
                        <label class="w3-left">{{ $tc('tokens.DAYS', assignation.config.maxDuration) }}</label>
                      </div>
                    </div>
                    <div class="w3-col m6">
                      <div class="w3-row">
                        <input v-model="assignation.config.selfBiasVisible" class="w3-check" type="checkbox">
                        <label>{{ $t('tokens.SELF_BIAS_VISIBLE') }}</label>
                      </div>
                      <div class="w3-row">
                        <input v-model="assignation.config.evaluationsVisible" class="w3-check" type="checkbox">
                        <label>{{ $t('tokens.ALL_EVALS_VISIBLE') }}</label>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </transition>
          </div>

          <hr>

          <div v-if="assetsZeroShow" class="w3-row error-panel w3-padding w3-round w3-margin-bottom">
            <div class="w3-col l10">
              {{ $t('tokens.NO_ASSETS_TRASFERRED') }}
            </div>
            <div class="w3-col l2 w3-center">
              <button
                class="w3-button app-button w3-round-large" name="button"
                @click="assetsEmptyErrorConfirmed = true">{{ $t('general.OK') }}</button>
            </div>
          </div>

          <div class="bottom-btns-row w3-row-padding">
            <div class="w3-col m6">
              <button type="button" class="w3-button app-button-light" @click="closeThis()">
                {{ $t('general.CANCEL') }}
              </button>
            </div>
            <div class="w3-col m6">
              <button type="button" class="w3-button app-button" @click="accept()">
                {{ $t('general.ACCEPT') }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'
import InitiativeAssetsAssigner from '@/components/transfers/InitiativeAssetsAssigner.vue'
import DirectAssignation from './support/DirectAssignation.vue'
import PeerReviewedAssignation from './support/PeerReviewedAssignation.vue'

export default {

  props: {
    initiative: {
      type: Object
    },
    assetId: {
      type: String
    }
  },

  components: {
    'app-initiative-assets-assigner': InitiativeAssetsAssigner,
    'app-direct-assignation': DirectAssignation,
    'app-peer-reviewed-assignation': PeerReviewedAssignation
  },

  data () {
    return {
      assignation: {
        type: this.DIRECT_ID(),
        motive: '',
        notes: '',
        config: {
          selfBiasVisible: true,
          evaluationsVisible: false,
          maxDuration: 8,
          minDuration: 2,
          startState: 'OPEN'
        }
      },
      motiveEmptyError: false,
      assetsZeroError: false,
      assetsAreZero: false,
      notEnoughReceivers: false,
      notEnoughEvaluators: false,
      allDonorsError: false,
      assetsEmptyErrorConfirmed: false,
      percentagesWrong: false
    }
  },

  computed: {
    isDirect () {
      return this.assignation.type === this.DIRECT_ID()
    },
    isPeerReviewed () {
      return this.assignation.type === this.PEER_REVIEWED_ID()
    },
    motiveErrorShow () {
      return this.motiveEmptyShow || this.motiveTooLarge
    },
    motiveEmptyShow () {
      return this.motiveEmptyError && this.assignation.motive === ''
    },
    motiveTooLarge () {
      return this.assignation.motive.length > 55
    },
    assetsZeroShow () {
      return this.assetsZeroError && this.assetsAreZero && !this.assetsEmptyErrorConfirmed
    },
    existNotDonors () {
      if (this.assignation.peerReviewReceivers) {
        for (var ix in this.assignation.peerReviewReceivers) {
          if (!this.assignation.peerReviewReceivers[ix].isDonor) {
            return true
          }
        }
      }
      return false
    },
    allDonorsShow () {
      return this.allDonorsError && !this.existNotDonors
    },
    isStartOnHold () {
      return this.assignation.config.startState === 'ON_HOLD'
    },
    isStartOpen () {
      return this.assignation.config.startState === 'OPEN'
    },
    sumOfPercentages () {
      if (this.assignation.directReceivers) {
        var sum = 0
        for (var ix in this.assignation.directReceivers) {
          sum += this.assignation.directReceivers[ix].percent
        }
        return sum
      } else {
        return 0
      }
    },
    sumOfPercentagesWrong () {
      return Math.abs(this.sumOfPercentages - 100) > 0.001
    },
    percentagesWrongShow () {
      return this.percentagesWrong && this.sumOfPercentagesWrong
    },
    popperOptions () {
      return {
        placement: 'bottom',
        modifiers: {
          preventOverflow: {
            enabled: true,
            boundariesElement: 'viewport'
          }
        }
      }
    }
  },

  methods: {
    ...mapActions(['showOutputMessage']),

    DIRECT_ID () {
      return 'DIRECT'
    },

    PEER_REVIEWED_ID () {
      return 'PEER_REVIEWED'
    },

    closeThis () {
      this.$emit('close')
    },
    assignationUpdated (assignation) {
      this.assignation = assignation
    },
    assetsSelected (assets) {
      this.assignation.assets = assets
      if (this.assetsZeroShow) {
        this.areAssetsZero()
        if (this.assetsAreZero) {
          this.assetsZeroError = false
        }
      }
    },
    areAssetsZero () {
      var result = true
      if (this.assignation.assets) {
        for (var ix in this.assignation.assets) {
          if (this.assignation.assets[ix].value !== 0) {
            result = false
          }
        }
      }
      this.assetsAreZero = result
    },
    receiversUpdated (data) {
      if (this.notEnoughReceivers) {
        if (data.receivers.length > 0) {
          this.notEnoughReceivers = false
        }
      }

      if (this.notEnoughEvaluators) {
        if (data.evaluators.length > 0) {
          this.notEnoughEvaluators = false
        }
      }

      if (this.allDonorsError) {
        if (this.existNotDonors) {
          this.allDonorsError = false
        }
      }

      if (this.isDirect) {
        this.directReceiversSelected(data)
      } else {
        this.peerReviewReceiversSelected(data)
      }
    },
    directReceiversSelected (data) {
      this.assignation.directReceivers = data.receivers
    },
    peerReviewReceiversSelected (data) {
      this.assignation.peerReviewReceivers = data.receivers
      this.assignation.peerReviewEvaluators = data.evaluators
    },
    accept () {
      /* validation */
      var ok = true
      if (this.assignation.motive === '') {
        this.motiveEmptyError = true
        ok = false
      }

      if (this.motiveTooLarge) {
        ok = false
      }

      this.areAssetsZero()
      if (this.assetsAreZero) {
        if (!this.assetsEmptyErrorConfirmed) {
          ok = false
          this.assetsZeroError = true
        }
      }

      if (this.isDirect) {
        if (!this.assignation.directReceivers) {
          this.notEnoughReceivers = true
          ok = false
        } else {
          if (this.assignation.directReceivers.length === 0) {
            this.notEnoughReceivers = true
            ok = false
          }
        }
        if (this.sumOfPercentagesWrong) {
          this.percentagesWrong = true
          ok = false
        }
      } else {
        if (!this.assignation.peerReviewReceivers) {
          this.notEnoughReceivers = true
          ok = false
        } else {
          if (this.assignation.peerReviewReceivers.length === 0) {
            this.notEnoughReceivers = true
            ok = false
          }

          if (!this.existNotDonors) {
            this.allDonorsError = true
            ok = false
          }
        }

        if (!this.assignation.peerReviewEvaluators) {
          this.notEnoughEvaluators = true
          ok = false
        } else {
          if (this.assignation.peerReviewEvaluators.length === 0) {
            this.notEnoughEvaluators = true
            ok = false
          }
        }
      }

      if (ok) {
        var assignationToSend = {}
        assignationToSend.type = this.assignation.type
        assignationToSend.assets = this.assignation.assets
        assignationToSend.initiativeId = this.assignation.assets[0].senderId
        assignationToSend.motive = this.assignation.motive
        assignationToSend.notes = this.assignation.notes

        assignationToSend.config = this.assignation.config

        if (this.isDirect) {
          assignationToSend.receivers = this.assignation.directReceivers
        } else {
          assignationToSend.receivers = this.assignation.peerReviewReceivers
          assignationToSend.evaluators = this.assignation.peerReviewEvaluators
        }

        this.axios.post('/1/initiative/' + assignationToSend.initiativeId + '/assignation', assignationToSend)
          .then((response) => {
            this.$store.commit('triggerUpdateAssets')
            this.$emit('created')
            this.closeThis()
          })
      }
    }
  }

}
</script>

<style scoped>

.this-container {
  padding-top: 10px;
  padding-bottom: 30px;
}

form {
  padding-top: 0px;
  padding-bottom: 35px;
}

.section-tabs {
  text-align: center;
  user-select: none;
  cursor: pointer;
}

.bold-text {
  font-weight: bold;
}

.assset-assigner {
  margin-bottom: 10px;
}

.configuration-row {
  margin-top: 8px;
  white-space: nowrap;
}

.configuration-row .w3-row {
  height: 40px !important;
}

.configuration-row .first-label {
  width: 100px;
}

.configuration-row .w3-col {
  margin-bottom: 10px;
}

.configuration-row label {
  font-size: 16px;
  margin-left: 10px;
}

.configuration-row .input-number {
  width: 70px;
  margin-left: 8px;
}

.configuration-row .state-btns {
  padding-top: 5px;
}

.configuration-row .state-btns .w3-button {
  height: 25px;
}

.configuration-row button {
  padding: 0px 16px;
  width: 90px;
  display: inline-block;
}

.configuration-row label {
  padding-top: 8px;
}

.bottom-btns-row button {
  width: 100%;
}

</style>
