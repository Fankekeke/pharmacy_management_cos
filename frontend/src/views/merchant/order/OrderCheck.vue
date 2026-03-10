
<template>
  <a-modal v-model="show" title="订单审核" @cancel="onClose" :width="800">
    <template slot="footer">
      <a-button key="back" @click="submit('pass')" type="primary">
        通过
      </a-button>
      <a-button @click="onClose">
        关闭
      </a-button>
    </template>
    <div style="font-size: 13px;font-family: SimHei" v-if="orderCheckData !== null">
      <a-row style="padding-left: 24px;padding-right: 24px;">
        <a-col style="margin-bottom: 15px"><span style="font-size: 15px;font-weight: 650;color: #000c17">基础信息</span></a-col>
        <a-col :span="8"><b>订单编号：</b>
          {{ orderCheckData.code }}
        </a-col>
      </a-row>
      <br/>
      <a-row style="padding-left: 24px;padding-right: 24px;">
        <a-col :span="8"><b>订单金额：</b>
          {{ orderCheckData.totalCost }} 元
        </a-col>
        <a-col :span="8"><b>下单时间：</b>
          {{ orderCheckData.createDate }}
        </a-col>
      </a-row>
      <br/>
      <a-row style="padding-left: 24px;padding-right: 24px;">
        <a-col style="margin-bottom: 15px"><span style="font-size: 15px;font-weight: 650;color: #000c17">药店信息</span></a-col>
        <a-col :span="8"><b>药店名称：</b>
          {{ orderCheckData.pharmacyName }}
        </a-col>
        <a-col :span="8"><b>药店地址：</b>
          {{ orderCheckData.address }}
        </a-col>
        <a-col :span="8"><b>联系方式：</b>
          {{ orderCheckData.pharmacyPhone }}
        </a-col>
      </a-row>
      <br/>
      <a-row style="padding-left: 24px;padding-right: 24px;">
        <a-col style="margin-bottom: 15px"><span style="font-size: 15px;font-weight: 650;color: #000c17">购买药品信息</span></a-col>
        <a-col :span="24">
          <a-table :columns="columns" :data-source="drugList">
          </a-table>
        </a-col>
      </a-row>
      <a-divider orientation="left">
        <span style="font-size: 12px;font-family: SimHei">审核备注</span>
      </a-divider>
      <a-row style="padding-left: 24px;padding-right: 24px;" :gutter="50">
        <a-col :span="24">
          <a-form-item label='审核意见' v-bind="formItemLayout">
            <a-textarea :rows="6" v-model="checkData.remark" placeholder="请输入审核意见"/>
          </a-form-item>
        </a-col>
      </a-row>
    </div>
  </a-modal>
</template>

<script>import moment from 'moment'
import {mapState} from 'vuex'
function getBase64 (file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = error => reject(error)
  })
}
moment.locale('zh-cn')
const formItemLayout = {
  labelCol: { span: 24 },
  wrapperCol: { span: 24 }
}
export default {
  name: 'OrderCheck',
  props: {
    orderCheckShow: {
      type: Boolean,
      default: false
    },
    orderCheckData: {
      type: Object
    }
  },
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    }),
    show: {
      get: function () {
        return this.orderCheckShow
      },
      set: function () {
      }
    },
    columns () {
      return [{
        title: '药品名称',
        dataIndex: 'drugName'
      }, {
        title: '品牌',
        dataIndex: 'brand'
      }, {
        title: '数量',
        dataIndex: 'quantity'
      }, {
        title: '药品图片',
        dataIndex: 'images',
        customRender: (text, record, index) => {
          if (!record.images) return <a-avatar shape="square" icon="user" />
          return <a-popover>
            <template slot="content">
              <a-avatar shape="square" size={132} icon="user" src={ 'http://127.0.0.1:9527/imagesWeb/' + record.images.split(',')[0] } />
            </template>
            <a-avatar shape="square" icon="user" src={ 'http://127.0.0.1:9527/imagesWeb/' + record.images.split(',')[0] } />
          </a-popover>
        }
      }, {
        title: '售价',
        dataIndex: 'unitPrice'
      }]
    }
  },
  watch: {
    'orderCheckShow': function (value) {
      if (value) {
        this.selectOrderDetail(this.orderCheckData.id)
      }
    }
  },
  data () {
    return {
      formItemLayout,
      loading: false,
      fileList: [],
      previewVisible: false,
      previewImage: '',
      checkData: {
        remark: ''
      },
      staffList: [],
      drugList: []
    }
  },
  methods: {
    moment,
    selectOrderDetail (orderId) {
      this.$get(`/cos/order-detail/detail/${orderId}`).then((r) => {
        this.drugList = r.data.data
      })
    },
    selectStaffByProduct (productId) {
      this.$get(`/cos/staff-info/work/${productId}`).then((r) => {
        this.staffList = r.data.data
      })
    },
    onDateChange (date) {
      this.checkData.reserveDate = moment(date).format('YYYY-MM-DD')
    },
    handleCancel () {
      this.previewVisible = false
    },
    async handlePreview (file) {
      if (!file.url && !file.preview) {
        file.preview = await getBase64(file.originFileObj)
      }
      this.previewImage = file.url || file.preview
      this.previewVisible = true
    },
    picHandleChange ({ fileList }) {
      this.fileList = fileList
    },
    imagesInit (images) {
      if (images !== null && images !== '') {
        let imageList = []
        images.split(',').forEach((image, index) => {
          imageList.push({uid: index, name: image, status: 'done', url: 'http://127.0.0.1:9527/imagesWeb/' + image})
        })
        this.fileList = imageList
      }
    },
    submit (action) {
      if (!this.checkData.remark || this.checkData.remark.trim() === '') {
        this.$message.warning('请填写审核意见')
        return
      }
      const endpoint = action === 'pass' ? 'audit-pass' : 'audit-reject'
      this.$get(`/cos/order-info/orderAudit`, {
        'orderId': this.orderCheckData.id,
        'aiRemark': this.checkData.remark
      }).then((r) => {
        this.cleanData()
        this.$emit('success')
      })
    },
    onClose () {
      this.cleanData()
      this.$emit('close')
    },
    cleanData () {
      this.checkData.remark = ''
    }
  }
}
</script>

<style scoped>
</style>
