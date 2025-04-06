<template>
  <div class="dashboard">
    <h2 class="dashboard__title">
      Corporate Office Risk Framework Dashboard
    </h2>
    <p class="dashboard__date-range">
      01/03/2023 - 31/03/2024
    </p>

    <el-row
      :gutter="20"
      class="dashboard__row"
    >
      <el-col
        :span="6"
        :xs="24"
        :sm="12"
        :md="6"
      >
        <health-score-gauge
          title="Corporate Office Health Score"
          :score="3.3"
          label="Moderate"
          sublabel="Risk Culture"
          :colors="colors"
        />
      </el-col>
      <el-col
        :span="18"
        :xs="24"
        :sm="12"
        :md="18"
      >
        <el-row :gutter="10">
          <el-col
            v-for="item in categoryScores"
            :key="item.name"
            :span="4"
            :xs="12"
            :sm="8"
            :md="6"
            :lg="4"
          >
            <kpi-card
              :title="`${item.name} Health Score`"
              :score="item.score"
              :description="item.description"
            />
          </el-col>
        </el-row>
      </el-col>
    </el-row>

    <el-row class="dashboard__row">
      <el-col :span="24">
        <el-card
          shadow="never"
          class="dashboard__card"
        >
          <div
            slot="header"
            class="dashboard__card-header"
          >
            <span>Filters</span>
            <div class="dashboard__filter-controls">
              <el-input
                v-model="tableSearch"
                placeholder="Search business units"
                prefix-icon="el-icon-search"
                clearable
                size="small"
                class="dashboard__search-input"
              />
            </div>
          </div>
          <div class="dashboard__filters">
            <el-tag
              size="small"
              class="dashboard__filter-tag"
            >
              Division: Corporate Office
            </el-tag>
            <el-tag
              size="small"
              class="dashboard__filter-tag"
            >
              BU: All
            </el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card
      shadow="never"
      class="dashboard__card"
    >
      <div
        slot="header"
        class="dashboard__card-header"
      >
        <span>Business Unit Details</span>
      </div>
      <el-table
        v-loading="tableLoading"
        :data="filteredBusinessUnitData"
        stripe
        size="mini"
        class="dashboard__table"
        @sort-change="handleSortChange"
      >
        <el-table-column
          prop="unit"
          label="Business Unit"
          width="180"
          sortable="custom"
          fixed
        />
        <el-table-column
          prop="score"
          label="Risk Health Score"
          width="100"
          align="center"
          sortable="custom"
        />
        <el-table-column
          prop="inherent"
          label="Inherent Rating"
          width="120"
          sortable="custom"
        />
        <el-table-column
          prop="residual"
          label="Residual Rating"
          width="120"
          sortable="custom"
        />
        <el-table-column
          prop="activeControls"
          label="Active Controls"
          width="100"
          align="center"
          sortable="custom"
        />
        <el-table-column
          prop="reviewedLast12"
          label="Reviewed Last 12 mths"
          width="100"
          align="center"
          sortable="custom"
        />
        <el-table-column
          prop="controlsTotal"
          label="Controls Total"
          width="100"
          align="center"
          sortable="custom"
        />
        <el-table-column
          prop="attestationsQuestions"
          label="Attestations Questions"
          width="100"
          align="center"
          sortable="custom"
        />
        <el-table-column
          prop="nonCompliance"
          label="Non Compliance"
          width="100"
          align="center"
          sortable="custom"
        />
        <el-table-column
          prop="kriCount"
          label="KRI Count"
          width="80"
          align="center"
          sortable="custom"
        />
        <el-table-column
          prop="kriRed"
          label="Red KRI"
          width="80"
          align="center"
          sortable="custom"
        />
        <el-table-column
          prop="actionsOpen"
          label="Actions Open"
          width="100"
          align="center"
          sortable="custom"
        />
        <el-table-column
          prop="actionsOverdue"
          label="Actions Overdue"
          width="100"
          align="center"
          sortable="custom"
        />
        <el-table-column
          prop="auditFindings"
          label="Audit Findings"
          width="100"
          align="center"
          sortable="custom"
        />
        <el-table-column
          prop="auditOverdue"
          label="Findings Overdue"
          width="100"
          align="center"
          sortable="custom"
        />
        <el-table-column
          prop="incidents"
          label="Incidents"
          width="80"
          align="center"
          sortable="custom"
        />
        <el-table-column
          prop="estimatedLoss"
          label="Estimated Direct Loss"
          width="150"
          align="right"
          sortable="custom"
        >
          <template slot-scope="scope">
            {{ formatCurrency(scope.row.estimatedLoss) }}
          </template>
        </el-table-column>
      </el-table>
      <div class="dashboard__pagination">
        <el-pagination
          :current-page="currentPage"
          :page-sizes="[5, 10, 20, 50]"
          :page-size="pageSize"
          :total="totalItems"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import HealthScoreGauge from '@/components/dashboard/HealthScoreGauge.vue';
import KpiCard from '@/components/dashboard/KpiCard.vue';

export default {
  name: 'DashboardComponent',
  components: {HealthScoreGauge, KpiCard},
  data() {
    return {
      colors: [
        {color: '#5cb87a', percentage: 20}, // Green
        {color: '#e6a23c', percentage: 40}, // Yellow
        {color: '#e6a23c', percentage: 60}, // Yellow (Moderate)
        {color: '#f56c6c', percentage: 80}, // Red
        {color: '#f56c6c', percentage: 100} // Red
      ],
      categoryScores: [
        {
          name: 'Risks',
          score: 3,
          description: 'Improve your risks health score by ensuring your risk assessments are up to date.'
        },
        {name: 'Controls', score: 1.2, description: 'Improve your controls health score by reviewing controls.'},
        {
          name: 'Attestations',
          score: 5,
          description: 'Improve your attestations health score by providing responses on time.'
        },
        {name: 'Actions', score: 1.9, description: 'Improve your action health score by closing actions.'},
        {
          name: 'Audit Findings',
          score: 0.1,
          description: 'Improve your audit findings health score by closing findings.'
        },
        {name: 'Incidents', score: 2, description: 'Improve health score by reducing the number of incidents.'},
      ],
      businessUnitData: [
        {
          unit: 'Call Centre',
          score: 5,
          inherent: 'High',
          residual: 'Moderate',
          activeControls: '',
          reviewedLast12: '',
          controlsTotal: '',
          attestationsQuestions: 22,
          nonCompliance: '',
          kriCount: 2,
          kriRed: '',
          actionsOpen: '',
          actionsOverdue: '',
          auditFindings: '',
          auditOverdue: '',
          incidents: '',
          estimatedLoss: ''
        },
        {
          unit: 'Corporate Office & Strategy',
          score: 3.3,
          inherent: 'High',
          residual: 'Moderate',
          activeControls: '',
          reviewedLast12: 1,
          controlsTotal: 3,
          attestationsQuestions: '',
          nonCompliance: '',
          kriCount: 128,
          kriRed: 11,
          actionsOpen: '',
          actionsOverdue: '',
          auditFindings: 2,
          auditOverdue: 1,
          incidents: '',
          estimatedLoss: ''
        },
        {
          unit: 'Finance',
          score: 4.4,
          inherent: 'Moderate',
          residual: 'Moderate',
          activeControls: '',
          reviewedLast12: '',
          controlsTotal: '',
          attestationsQuestions: '',
          nonCompliance: '',
          kriCount: 77,
          kriRed: 7,
          actionsOpen: 25,
          actionsOverdue: 25,
          auditFindings: '',
          auditOverdue: '',
          incidents: '',
          estimatedLoss: ''
        },
        {
          unit: 'Health and Safety',
          score: 5,
          inherent: 'High',
          residual: 'Moderate',
          activeControls: '',
          reviewedLast12: '',
          controlsTotal: '',
          attestationsQuestions: '',
          nonCompliance: '',
          kriCount: 33,
          kriRed: 5,
          actionsOpen: 15,
          actionsOverdue: '',
          auditFindings: '',
          auditOverdue: '',
          incidents: '',
          estimatedLoss: ''
        },
        {
          unit: 'Information Technology',
          score: 2.1,
          inherent: 'High',
          residual: 'Moderate',
          activeControls: 4,
          reviewedLast12: '',
          controlsTotal: 84,
          attestationsQuestions: 136,
          nonCompliance: '',
          kriCount: 12,
          kriRed: 85,
          actionsOpen: 81,
          actionsOverdue: 7,
          auditFindings: 3,
          auditOverdue: 1,
          incidents: '',
          estimatedLoss: ''
        },
        {
          unit: 'Marketing',
          score: 5,
          inherent: 'Moderate',
          residual: 'Moderate',
          activeControls: '',
          reviewedLast12: '',
          controlsTotal: '',
          attestationsQuestions: 44,
          nonCompliance: '',
          kriCount: 4,
          kriRed: '',
          actionsOpen: '',
          actionsOverdue: '',
          auditFindings: '',
          auditOverdue: '',
          incidents: '',
          estimatedLoss: ''
        },
        {
          unit: 'Operations',
          score: 3.3,
          inherent: 'High',
          residual: 'Low',
          activeControls: '',
          reviewedLast12: '',
          controlsTotal: '',
          attestationsQuestions: 122,
          nonCompliance: '',
          kriCount: 10,
          kriRed: 5,
          actionsOpen: 5,
          actionsOverdue: '',
          auditFindings: '',
          auditOverdue: '',
          incidents: 1,
          estimatedLoss: ''
        },
        {
          unit: 'People & Culture',
          score: 0.7,
          inherent: 'High',
          residual: 'Moderate',
          activeControls: '',
          reviewedLast12: '',
          controlsTotal: 73,
          attestationsQuestions: 99,
          nonCompliance: '',
          kriCount: 9,
          kriRed: 9,
          actionsOpen: 5,
          actionsOverdue: '',
          auditFindings: '',
          auditOverdue: '',
          incidents: '',
          estimatedLoss: ''
        },
        {
          unit: 'Product',
          score: 2,
          inherent: 'High',
          residual: 'Moderate',
          activeControls: 2,
          reviewedLast12: '',
          controlsTotal: '',
          attestationsQuestions: '',
          nonCompliance: '',
          kriCount: '',
          kriRed: '',
          actionsOpen: 3,
          actionsOverdue: 3,
          auditFindings: '',
          auditOverdue: '',
          incidents: '',
          estimatedLoss: ''
        },
        {
          unit: 'Regulatory Compliance',
          score: 5,
          inherent: 'High',
          residual: 'Low',
          activeControls: 2,
          reviewedLast12: 1,
          controlsTotal: 15,
          attestationsQuestions: 18,
          nonCompliance: '',
          kriCount: 2,
          kriRed: 12,
          actionsOpen: 10,
          actionsOverdue: 6,
          auditFindings: 6,
          auditOverdue: 7,
          incidents: 7,
          estimatedLoss: 2000000
        },
        {
          unit: 'Risk',
          score: 0,
          inherent: 'High',
          residual: 'Moderate',
          activeControls: 2,
          reviewedLast12: 1,
          controlsTotal: 32,
          attestationsQuestions: 22,
          nonCompliance: '',
          kriCount: '',
          kriRed: '',
          actionsOpen: 1,
          actionsOverdue: '',
          auditFindings: '',
          auditOverdue: '',
          incidents: '',
          estimatedLoss: ''
        },
        {
          unit: 'Treasury',
          score: 0,
          inherent: 'High',
          residual: 'High',
          activeControls: 1,
          reviewedLast12: '',
          controlsTotal: '',
          attestationsQuestions: 78,
          nonCompliance: '',
          kriCount: 7,
          kriRed: 2,
          actionsOpen: 2,
          actionsOverdue: 1,
          auditFindings: 1,
          auditOverdue: 1,
          incidents: 1,
          estimatedLoss: 139000
        },
      ],
      tableLoading: false,
      tableSearch: '',
      currentPage: 1,
      pageSize: 10,
      sortBy: 'unit',
      sortOrder: 'ascending'
    };
  },
  computed: {
    filteredBusinessUnitData() {
      let filtered = this.tableSearch
          ? this.businessUnitData.filter(item =>
              item.unit.toLowerCase().includes(this.tableSearch.toLowerCase()))
          : this.businessUnitData;

      return [...filtered].sort((a, b) => {
        const aValue = a[this.sortBy] !== undefined ? a[this.sortBy] : '';
        const bValue = b[this.sortBy] !== undefined ? b[this.sortBy] : '';

        if (!isNaN(aValue) && !isNaN(bValue)) {
          return this.sortOrder === 'ascending'
              ? Number(aValue) - Number(bValue)
              : Number(bValue) - Number(aValue);
        }
        return this.sortOrder === 'ascending'
            ? String(aValue).localeCompare(String(bValue))
            : String(bValue).localeCompare(String(aValue));
      }).slice((this.currentPage - 1) * this.pageSize, this.currentPage * this.pageSize);
    },
    totalItems() {
      return this.tableSearch
          ? this.businessUnitData.filter(item =>
              item.unit.toLowerCase().includes(this.tableSearch.toLowerCase())).length
          : this.businessUnitData.length;
    }
  },
  mounted() {
    this.loadData();
  },
  methods: {
    formatCurrency(value) {
      return value || value === 0 ? `$ ${Number(value).toLocaleString()}` : '';
    },
    handleSizeChange(size) {
      this.pageSize = size;
    },
    handleCurrentChange(page) {
      this.currentPage = page;
    },
    handleSortChange({prop, order}) {
      this.sortBy = prop;
      this.sortOrder = order;
    },
    loadData() {
      this.tableLoading = true;
      setTimeout(() => (this.tableLoading = false), 500);
    }
  }
};
</script>

<style scoped>
.dashboard {
  padding: 0 10px;
}

.dashboard__title {
  margin-bottom: 10px;
}

.dashboard__date-range {
  color: grey;
  margin-bottom: 20px;
}

.dashboard__row {
  margin-bottom: 20px;
}

.dashboard__card {
  margin-bottom: 20px;
}

.dashboard__card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dashboard__filters {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.dashboard__filter-tag {
  margin-right: 5px;
}

.dashboard__filter-controls {
  display: flex;
  align-items: center;
}

.dashboard__search-input {
  width: 200px;
}

.dashboard__table {
  width: 100%;
}

.dashboard__pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .dashboard__search-input {
    width: 100%;
  }

  .dashboard__card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .dashboard__filter-controls {
    margin-top: 10px;
    width: 100%;
  }
}
</style>
