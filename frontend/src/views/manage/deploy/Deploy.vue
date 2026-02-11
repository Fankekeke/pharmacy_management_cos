<template>
  <div class="deploy-container" style="width: 100%">
    <!-- 选择目标药店 -->
    <div class="pharmacy-selection">
      <h3>请选择目标药店：</h3>
      <a-row :gutter="16">
        <a-col
          v-for="pharmacy in pharmacyList"
          :key="pharmacy.id"
          :span="6"
        >
          <a-card
            hoverable
            :class="{ selected: selectedPharmacyId === pharmacy.id }"
            @click="selectTargetPharmacy(pharmacy.id)"
          >
            <a-card-meta :title="pharmacy.name" />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 查询可调配的药房 -->
    <a-button @click="loadAllocatablePharmacies" style="margin-top: 20px;">查询可调配药房</a-button>

    <!-- 选择源药房 -->
    <div class="pharmacy-selection" v-if="pharmacyAllocate.length > 0">
      <h3>请选择源药房：</h3>
      <a-row :gutter="16">
        <a-col
          v-for="pharmacy in pharmacyAllocate"
          :key="pharmacy.id"
          :span="6"
        >
          <a-card
            hoverable
            :class="{ selected: selectedSourcePharmacyId === pharmacy.id }"
            @click="selectSourcePharmacy(pharmacy.id)"
          >
            <a-card-meta :title="pharmacy.name" />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 查询目标药店库存 -->
    <a-button @click="loadTargetInventory" style="margin-top: 20px;">查询目标药店库存</a-button>
    <a-table
      v-if="pharmacyInventory.length !== 0"
      :dataSource="pharmacyInventory"
      :columns="columns"
      rowKey="id"
      :pagination="false"      style="margin-top: 20px;"
    >
      <template slot="operation" slot-scope="text, record">
        <a-checkbox v-model:checked="record.selected" />
      </template>
    </a-table>

    <!-- 批量提交调配 -->
    <a-button type="primary" @click="submitAllocation" style="margin-top: 20px;">提交调配</a-button>
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
    selectTargetPharmacy(pharmacyId) {
      this.selectedPharmacyId = pharmacyId;
      this.pharmacyAllocate = []; // 清空可调配药房列表
      this.pharmacyInventory = []; // 清空库存信息
    },

    // 新增方法：选择源药房
    selectSourcePharmacy(pharmacyId) {
      this.selectedSourcePharmacyId = pharmacyId;
    },
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

      let medicindes = selectedMedicines.map(item => ({
        medicineId: item.id,
        quantity: item.allocateQuantity // 使用用户输入的调配数量
      }))
      const payload = {
        targetPharmacyId: this.selectedPharmacyId,
        sourcePharmacyId: this.selectedSourcePharmacyId,
        medicines: JSON.stringify(medicindes)
      };

      // console.log(JSON.stringify(payload)); // 调试输出
      // return false
      this.$post('/cos/pharmacy-inventory/dispatch', payload).then(() => {
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

.deploy-container {
  padding: 20px;
}

.pharmacy-selection {
  margin-top: 20px;
}

.pharmacy-selection .ant-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.pharmacy-selection .ant-card.selected {
  border-color: #1890ff;
  box-shadow: 0 0 10px rgba(24, 144, 255, 0.3);
}

.pharmacy-selection .ant-card {
  border-radius: 8px; /* 圆角 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); /* 阴影 */
  transition: all 0.3s ease;
}

.pharmacy-selection .ant-card:hover {
  transform: translateY(-4px); /* 悬停时上移 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); /* 更深的阴影 */
}

.pharmacy-selection .ant-card.selected {
  border-color: #1890ff;
  background-color: #e6f7ff; /* 选中时的浅蓝色背景 */
  box-shadow: 0 0 10px rgba(24, 144, 255, 0.3);
}


/* 斑马纹效果 */
.ant-table-tbody > tr:nth-child(even) {
  background-color: #fafafa;
}

/* 表头样式 */
.ant-table-thead > tr > th {
  background-color: #f0f2f5;
  font-weight: bold;
  text-align: center;
}

/* 表格内容居中对齐 */
.ant-table-tbody > tr > td {
  text-align: center;
}

/* 复选框样式调整 */
.ant-checkbox-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}


/* 默认按钮样式 */
.ant-btn {
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.ant-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

/* 主按钮样式 */
.ant-btn-primary {
  background-color: #1890ff;
  border-color: #1890ff;
}

.ant-btn-primary:hover {
  background-color: #40a9ff;
  border-color: #40a9ff;
}
</style>
