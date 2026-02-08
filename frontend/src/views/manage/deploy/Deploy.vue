
<template>
  <div class="deploy-container" style="width: 100%">
    <!-- 选择当前药店 -->
    <a-select style="width: 300px" v-model:value="selectedPharmacyId" placeholder="请选择目标药店" @change="onPharmacyChange">
      <a-select-option
        v-for="pharmacy in pharmacyList"
        :key="pharmacy.id"
        :value="pharmacy.id"
      >
        {{ pharmacy.name }}
      </a-select-option>
    </a-select>

    <!-- 查询可调配的药房 -->
    <a-button @click="loadAllocatablePharmacies">查询可调配药房</a-button>
    <a-select  style="width: 300px" v-model:value="selectedSourcePharmacyId" placeholder="请选择源药房">
      <a-select-option
        v-for="pharmacy in pharmacyAllocate"
        :key="pharmacy.id"
        :value="pharmacy.id"
      >
        {{ pharmacy.name }}
      </a-select-option>
    </a-select>

    <!-- 查询目标药店库存 -->
    <a-button @click="loadTargetInventory">查询目标药店库存</a-button>
    <a-table
      v-if="pharmacyInventory.length !== 0"
      :dataSource="pharmacyInventory"
      :columns="columns"
      rowKey="id"
      :pagination="false"
    >
      <template slot="operation" slot-scope="text, record">
        <a-checkbox v-model:checked="record.selected" />
      </template>
    </a-table>

    <!-- 批量提交调配 -->
    <a-button type="primary" @click="submitAllocation" style="margin-top: 10px">提交调配</a-button>
  </div>
</template>

<script>export default {
  name: 'Deploy',
  data () {
    return {
      columns: [
        {
          title: '药品名称',
          dataIndex: 'drugName',
          key: 'drugName'
        },
        {
          title: '当前库存',
          dataIndex: 'reserve',
          key: 'reserve'
        },
        {
          title: '品牌',
          dataIndex: 'brand',
          key: 'brand'
        },
        {
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
        },
        {
          title: '剂型',
          dataIndex: 'dosageForm',
          key: 'dosageForm'
        },
        {
          title: '单价',
          dataIndex: 'unitPrice',
          key: 'unitPrice'
        },
        {
          title: '调配数量',
          key: 'allocateQuantity',
          customRender: (text, record, index) => {
            return (
              <a-input-number
                min={0}
                max={record.reserve} // 最大值不超过当前库存
                value={record.allocateQuantity || 0}
                onChange={(value) => {
                  this.$set(record, 'allocateQuantity', value);
                }}
              />
            );
          }
        },
        {
          title: '操作',
          key: 'action',
          scopedSlots: { customRender: 'operation' }
        }
      ],
      pharmacyList: [], // 所有药店列表
      selectedPharmacyId: null, // 目标药店ID
      pharmacyAllocate: [], // 可调配药房列表
      selectedSourcePharmacyId: null, // 源药房ID
      pharmacyInventory: [] // 目标药店库存信息
    }
  },
  mounted () {
    this.queryPharmacyList() // 初始化加载药店列表
  },
  methods: {
    // 加载所有药店列表
    queryPharmacyList () {
      this.$get('/cos/pharmacy-info/list').then((res) => {
        this.pharmacyList = res.data.data
      })
    },

    // 当选择目标药店时触发
    onPharmacyChange (pharmacyId) {
      this.selectedPharmacyId = pharmacyId
      this.pharmacyAllocate = [] // 清空可调配药房列表
      this.pharmacyInventory = [] // 清空库存信息
    },

    // 查询可调配药房
    loadAllocatablePharmacies () {
      if (!this.selectedPharmacyId) {
        this.$message.warning('请先选择目标药店')
        return
      }
      this.queryPharmacyAllocate(this.selectedPharmacyId)
    },

    // 查询可调配药房信息
    queryPharmacyAllocate (pharmacyId) {
      this.$get('/cos/pharmacy-info/queryPharmacyAllocate/', { pharmacyId }).then((res) => {
        this.pharmacyAllocate = res.data.data
      })
    },

    // 查询目标药店库存
    loadTargetInventory () {
      if (!this.selectedPharmacyId) {
        this.$message.warning('请先选择目标药店')
        return
      }
      this.queryPharmacyInventory(this.selectedPharmacyId)
    },

    // 查询目标药店库存信息
    queryPharmacyInventory (pharmacyId) {
      this.$get(`/cos/pharmacy-inventory/detail/pharmacy/${pharmacyId}`).then((res) => {
        this.pharmacyInventory = res.data.data.map(item => ({
          ...item,
          allocateQuantity: 0,
          selected: false // 添加选中状态字段
        }))
      })
    },

    // 提交调配
    submitAllocation () {
      if (!this.selectedPharmacyId || !this.selectedSourcePharmacyId) {
        this.$message.warning('请先选择目标药店和源药房');
        return;
      }

      if (!this.pharmacyInventory || this.pharmacyInventory.length === 0) {
        this.$message.warning('请先查询目标药店库存');
        return;
      }

      const selectedMedicines = this.pharmacyInventory.filter(
        item => item.selected && item.allocateQuantity > 0
      );

      if (selectedMedicines.length === 0) {
        this.$message.warning('请至少选择一种药品并填写调配数量');
        return;
      }

      const payload = {
        targetPharmacyId: this.selectedPharmacyId,
        sourcePharmacyId: this.selectedSourcePharmacyId,
        medicines: selectedMedicines.map(item => ({
          medicineId: item.id,
          quantity: item.allocateQuantity // 使用用户输入的调配数量
        }))
      };

      console.log(JSON.stringify(payload)); // 调试输出
      return false
      this.$post('/cos/inventory-allocation/allocate', payload).then(() => {
        this.$message.success('调配成功')
        this.loadTargetInventory() // 刷新库存信息
      })
    }
  }
}
</script>

<style scoped>.deploy-container {
  padding: 20px;
}

.el-select,
.el-button {
  margin-right: 10px;
  margin-bottom: 20px;
}
</style>
