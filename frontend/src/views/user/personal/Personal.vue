<template>
  <a-row :gutter="8" style="width: 100%">
    <a-col :span="8">
      <div style="background:#ECECEC; padding:30px;margin-top: 30px">
        <a-card :bordered="false">
          <b style="font-size: 15px">我的信息</b>
        </a-card>
        <a-card :bordered="false">
          <a-form :form="form" layout="vertical">
            <a-row :gutter="20">
              <a-col :span="12">
                <a-form-item label='用户姓名' v-bind="formItemLayout">
                  <a-input disabled v-decorator="[
                  'name',
                  { rules: [{ required: true, message: '请输入用户姓名!' }] }
                  ]"/>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label='联系电话' v-bind="formItemLayout">
                  <a-input v-decorator="[
                  'phone',
                  { rules: [{ required: true, message: '请输入联系电话!' }] }
                  ]"/>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label='收货地址' v-bind="formItemLayout">
                  <a-input v-decorator="[
                  'address',
                  { rules: [{ required: true, message: '请输入收货地址!' }] }
                  ]"/>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label='城市' v-bind="formItemLayout">
                  <a-input v-decorator="[
                  'city',
                  { rules: [{ required: true, message: '请输入城市!' }] }
                  ]"/>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label='区域' v-bind="formItemLayout">
                  <a-input v-decorator="[
                  'area',
                  { rules: [{ required: true, message: '请输入区域!' }] }
                  ]"/>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label='省份' v-bind="formItemLayout">
                  <a-input v-decorator="[
                  'province',
                  { rules: [{ required: true, message: '请输入省份!' }] }
                  ]"/>
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <div id="areas" class="map-container"></div>
                <div style="margin-top: 10px; color: #666; font-size: 12px">
                  <a-alert
                    v-if="deliveryWarning"
                    message="所选地址超出 20 公里配送范围，无法配送"
                    type="warning"
                    showIcon
                    closable
                  />
                  <a-alert
                    v-else-if="selectedPoint"
                    message="已选择地图位置，将自动填充到收货地址"
                    type="success"
                    showIcon
                    closable
                  />
                </div>
              </a-col>
              <a-col :span="24">
                <a-form-item>
                  <a-button
                    size="large"
                    type="primary"
                    htmlType="submit"
                    class="register-button"
                    style="width: 35%;float: right;margin-top: 50px"
                    :loading="loading"
                    @click.stop.prevent="handleSubmit"
                    :disabled="loading">立即修改
                  </a-button>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-card>
      </div>
    </a-col>
    <a-col :span="16">
      <div style="background:#ECECEC; padding:30px;margin-top: 30px">
        <a-card :bordered="false">
          <a-spin :spinning="dataLoading">
            <a-calendar>
              <ul slot="dateCellRender" slot-scope="value" class="events">
                <li v-for="item in getListData(value)" :key="item.content">
                  <a-badge :status="item.type" :text="item.content" />
                </li>
              </ul>
            </a-calendar>
          </a-spin>
        </a-card>
      </div>
    </a-col>
  </a-row>
</template>

<script>
import {mapState} from 'vuex'
import moment from 'moment'
moment.locale('zh-cn')
const formItemLayout = {
  labelCol: { span: 24 },
  wrapperCol: { span: 24 }
}
export default {
  name: 'User',
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    })
  },
  data () {
    return {
      map: null,
      mapId: 'areas',
      rowId: null,
      formItemLayout,
      form: this.$form.createForm(this),
      loading: false,
      pharmacyList: [],
      courseInfo: [],
      dataLoading: false,
      selectedPoint: null,
      deliveryWarning: false,
      geocoder: null,
      pharmacyMarkers: []
    }
  },
  mounted () {
    this.dataInit()
    setTimeout(() => {
      this.initMap()
    }, 200)
  },
  methods: {
    initMap () {
      this.map = new BMapGL.Map(this.mapId)
      this.map.centerAndZoom(new BMapGL.Point(116.404, 39.915), 12)
      this.map.enableScrollWheelZoom(true)
      this.geocoder = new BMapGL.Geocoder()
      this.addMapEventListeners()
      this.$get(`/cos/pharmacy-info/list`).then((r) => {
        this.pharmacyList = r.data.data
        if (this.map) {
          this.loadPharmacyMarkers()
        }
      })
    },
    addMapEventListeners () {
      this.map.addEventListener('click', (e) => {
        const point = new BMapGL.Point(e.latlng.lng, e.latlng.lat)
        this.selectedPoint = point
        this.getReverseGeocode(point)
        this.checkDeliveryRange(point)
      })
    },
    loadPharmacyMarkers () {
      if (!this.map || !this.pharmacyList || this.pharmacyList.length === 0) {
        console.log(111)
        return
      }
      this.clearPharmacyMarkers()
      const bounds = []
      console.log(123)
      this.pharmacyList.forEach((pharmacy, index) => {
        if (pharmacy.latitude && pharmacy.longitude) {
          const point = new BMapGL.Point(pharmacy.longitude, pharmacy.latitude)
          bounds.push(point)

          const marker = new BMapGL.Marker(point)

          const label = new BMapGL.Label(pharmacy.name, {
            position: point,
            offset: new BMapGL.Size(-20, -30)
          })
          label.setStyle({
            color: '#fff',
            fontSize: '12px',
            height: '20px',
            lineHeight: '20px',
            fontFamily: '微软雅黑',
            backgroundColor: '#1890ff',
            padding: '4px 8px',
            borderRadius: '4px',
            border: 'none',
            boxShadow: '0 2px 6px rgba(0,0,0,0.3)'
          })

          const infoWindow = new BMapGL.InfoWindow(
            `<div style="padding: 10px; font-size: 12px;">
              <p style="font-weight: bold; margin-bottom: 8px;">${pharmacy.name}</p>
              <p><b>地址:</b> ${pharmacy.address || '暂无'}</p>
              <p><b>电话:</b> ${pharmacy.phone || '暂无'}</p>
              <p><b>营业时间:</b> ${pharmacy.businessHours || '暂无'}</p>
            </div>`,
            { width: 280, height:120 }
          )
          marker.addEventListener('click', () => {
            marker.openInfoWindow(infoWindow)
          })
          marker.setLabel(label)
          this.map.addOverlay(marker)
          this.pharmacyMarkers.push(marker)
        }
      })

      if (bounds.length > 0) {
        const viewport = this.map.getViewport(bounds)
        this.map.setViewport(viewport)
      }
    },
    clearPharmacyMarkers () {
      if (this.pharmacyMarkers && this.pharmacyMarkers.length > 0) {
        this.pharmacyMarkers.forEach(marker => {
          this.map.removeOverlay(marker)
        })
        this.pharmacyMarkers = []
      }
    },
    getReverseGeocode (point) {
      this.geocoder.getLocation(point, (result) => {
        if (result) {
          const address = result.address
          const province = result.addressComponents.province
          const city = result.addressComponents.city
          const district = result.addressComponents.district

          this.form.setFieldsValue({
            address: address,
            province: province,
            city: city,
            area: district
          })
        }
      })
    },
    checkDeliveryRange (selectedPoint) {
      if (!this.pharmacyList || this.pharmacyList.length === 0) {
        this.deliveryWarning = true
        return
      }

      let withinRange = false

      for (let pharmacy of this.pharmacyList) {
        if (pharmacy.latitude && pharmacy.longitude) {
          const pharmacyPoint = new BMapGL.Point(pharmacy.longitude, pharmacy.latitude)
          console.log(selectedPoint)
          console.log(pharmacyPoint)
          const distance = this.map.getDistance(selectedPoint, pharmacyPoint)
          console.log('距离：' + distance)
          if (distance <= 20000) {
            withinRange = true
            break
          }
        }
      }

      this.deliveryWarning = !withinRange

      if (withinRange) {
        this.$message.success('该地址在配送范围内')
      } else {
        this.$message.warning('该地址超出配送范围 20 公里')
      }
    },
    isDuringDate (beginDateStr, endDateStr, curDataStr) {
      let curDate = new Date(curDataStr)
      let beginDate = new Date(beginDateStr)
      let endDate = new Date(endDateStr)
      if (curDate >= beginDate && curDate <= endDate) {
        return true
      }
      return false
    },
    getListData (value) {
      let listData = []
      this.courseInfo.forEach(item => {
        if ((moment(value).format('YYYY-MM-DD')) === (moment(item.createDate).format('YYYY-MM-DD'))) {
          listData.push({type: 'success', content: item.remark})
        }
      })
      return listData || []
    },
    dataInit () {
      this.dataLoading = true
      this.$get(`/cos/user-info/detail/${this.currentUser.userId}`).then((r) => {
        this.rowId = r.data.user.id
        this.setFormValues(r.data.user)
        this.courseInfo = r.data.order
        this.dataLoading = false
      })
    },
    setFormValues ({...student}) {
      this.rowId = student.id
      let fields = ['name', 'phone', 'address', 'province', 'city', 'area']
      let obj = {}
      Object.keys(student).forEach((key) => {
        if (fields.indexOf(key) !== -1) {
          this.form.getFieldDecorator(key)
          obj[key] = student[key]
        }
        if (key === 'sex' && student[key] !== null) {
          obj[key] = student[key].toString()
        }
      })
      this.form.setFieldsValue(obj)
    },
    handleSubmit () {
      this.form.validateFields((err, values) => {
        values.id = this.rowId
        if (!err) {
          this.loading = true
          this.$put('/cos/user-info', {
            ...values
          }).then((r) => {
            this.$message.success('修改信息成功')
            this.loading = false
          }).catch(() => {
            this.loading = false
          })
        }
      })
    }
  }
}
</script>

<style scoped>
#areas {
  width: 100%;
  height: 300px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e8e8e8;
}
</style>
