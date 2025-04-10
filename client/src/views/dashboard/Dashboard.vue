<template>
  <div class="dashboard">
    <h2 class="dashboard__title">
      KRI Dashboard <span class="dashboard__date-range">01/03/2023 - 31/03/2024</span>
    </h2>

    <el-row :gutter="20">
      <!-- Left side content -->
      <el-col
        :span="18"
        :xs="24"
        :sm="24"
        :md="18"
      >
        <!-- Top section with gauge and KPI cards -->
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
              title="Risk Health Score"
              :score="3.3"
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
                  :color="getScoreColor(item.score)"
                  :body-style="{ padding: '15px', height: '192px' }"
                />
              </el-col>
            </el-row>
          </el-col>
        </el-row>

        <!-- Bottom section with table -->
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
              sortable="custom"
              min-width="180"
            />
            <el-table-column
              prop="score"
              label="Score"
              sortable="custom"
              width="100"
            >
              <template slot-scope="scope">
                <span :style="{ color: getScoreColor(scope.row.score) }">
                  {{ scope.row.score }}
                </span>
              </template>
            </el-table-column>
            <el-table-column
              prop="inherent"
              label="Inherent Risk"
              sortable="custom"
              width="120"
            />
            <el-table-column
              prop="residual"
              label="Residual Risk"
              sortable="custom"
              width="120"
            />
            <el-table-column
              prop="activeControls"
              label="Active Controls"
              sortable="custom"
              width="120"
            />
            <el-table-column
              prop="reviewedLast12"
              label="Reviewed Last 12m"
              sortable="custom"
              width="150"
            />
            <el-table-column
              prop="controlsTotal"
              label="Controls Total"
              sortable="custom"
              width="120"
            />
            <el-table-column
              prop="attestationsQuestions"
              label="Attestations Questions"
              sortable="custom"
              width="180"
            />
            <el-table-column
              prop="nonCompliance"
              label="Non-Compliance"
              sortable="custom"
              width="140"
            />
            <el-table-column
              prop="kriCount"
              label="KRI Count"
              sortable="custom"
              width="100"
            />
            <el-table-column
              prop="kriRed"
              label="KRI Red"
              sortable="custom"
              width="100"
            />
            <el-table-column
              prop="actionsOpen"
              label="Actions Open"
              sortable="custom"
              width="120"
            />
            <el-table-column
              prop="actionsOverdue"
              label="Actions Overdue"
              sortable="custom"
              width="140"
            />
            <el-table-column
              prop="auditFindings"
              label="Audit Findings"
              sortable="custom"
              width="120"
            />
            <el-table-column
              prop="auditOverdue"
              label="Audit Overdue"
              sortable="custom"
              width="120"
            />
            <el-table-column
              prop="incidents"
              label="Incidents"
              sortable="custom"
              width="100"
            />
            <el-table-column
              prop="estimatedLoss"
              label="Estimated Loss"
              sortable="custom"
              width="120"
            >
              <template slot-scope="scope">
                {{ scope.row.estimatedLoss ? `$${Number(scope.row.estimatedLoss).toLocaleString()}` : '' }}
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
              @current-change="handlePageChange"
            />
          </div>
        </el-card>
      </el-col>

      <!-- Right side filters -->
      <el-col
        :span="6"
        :xs="24"
        :sm="24"
        :md="6"
      >
        <el-card
          shadow="never"
          class="dashboard__card dashboard__filters-card"
        >
          <div
            slot="header"
            class="dashboard__card-header"
          >
            <span>Filters</span>
          </div>
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
  </div>
</template>

<script>
import HealthScoreGauge from './HealthScoreGauge.vue';
import KpiCard from './KpiCard.vue';

export default {
  name: 'Dashboard',
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
        {name: 'Credit', score: 3.5, description: 'Credit risk is well managed with some areas for improvement'},
        {name: 'Market', score: 4.2, description: 'Market risk controls are effective'},
        {name: 'Operational', score: 2.8, description: 'Operational risk requires attention'},
        {name: 'Liquidity', score: 3.9, description: 'Liquidity risk is adequately managed'},
        {name: 'Compliance', score: 1.7, description: 'Compliance risk needs immediate attention'},
        {name: 'Strategic', score: 3.1, description: 'Strategic risk is within acceptable levels'}
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
          score: 4.5,
          inherent: 'High',
          residual: 'Low',
          activeControls: 3,
          reviewedLast12: 2,
          controlsTotal: 12,
          attestationsQuestions: 44,
          nonCompliance: '',
          kriCount: 6,
          kriRed: '',
          actionsOpen: 2,
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
          score: 3.8,
          inherent: 'High',
          residual: 'Moderate',
          activeControls: 2,
          reviewedLast12: 1,
          controlsTotal: 32,
          attestationsQuestions: 22,
          nonCompliance: '',
          kriCount: 8,
          kriRed: 2,
          actionsOpen: 2,
          actionsOverdue: 1,
          auditFindings: 1,
          auditOverdue: 1,
          incidents: 1,
          estimatedLoss: 139000
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
      let data = [...this.businessUnitData];
      
      // Apply search filter
      if (this.tableSearch) {
        const search = this.tableSearch.toLowerCase();
        data = data.filter(item => 
          item.unit.toLowerCase().includes(search)
        );
      }
      
      // Apply sorting
      data.sort((a, b) => {
        const aValue = a[this.sortBy];
        const bValue = b[this.sortBy];
        
        if (typeof aValue === 'string' && typeof bValue === 'string') {
          return this.sortOrder === 'ascending' 
            ? aValue.localeCompare(bValue) 
            : bValue.localeCompare(aValue);
        } else {
          // Handle numeric or empty values
          const aNum = aValue === '' ? -Infinity : Number(aValue);
          const bNum = bValue === '' ? -Infinity : Number(bValue);
          
          return this.sortOrder === 'ascending' ? aNum - bNum : bNum - aNum;
        }
      });
      
      return data;
    },
    
    totalItems() {
      return this.filteredBusinessUnitData.length;
    },
    
    paginatedData() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredBusinessUnitData.slice(start, end);
    }
  },
  methods: {
    getScoreColor(score) {
      if (score >= 4) return '#67C23A'; // Green
      if (score >= 3) return '#E6A23C'; // Yellow
      if (score >= 2) return '#F56C6C'; // Red
      return '#909399'; // Gray for 0-1 or no score
    },
    
    handleSortChange({ prop, order }) {
      this.sortBy = prop;
      this.sortOrder = order;
    },
    
    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1;
    },
    
    handlePageChange(page) {
      this.currentPage = page;
    }
  }
};
</script>

<style lang="scss" scoped>
.dashboard {
  &__title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 500;
  }
  
  &__date-range {
    font-size: 16px;
    font-weight: normal;
    color: #909399;
    margin-left: 10px;
  }
  
  &__row {
    margin-bottom: 20px;
  }
  
  &__card {
    margin-bottom: 20px;
    
    &-header {
      font-weight: 500;
    }
  }
  
  &__table {
    margin-bottom: 15px;
  }
  
  &__pagination {
    text-align: right;
  }
  
  &__filters-card {
    height: 100%;
  }
  
  &__filter-controls {
    margin-bottom: 15px;
  }
  
  &__search-input {
    width: 100%;
    margin-bottom: 15px;
  }
  
  &__filters {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  &__filter-tag {
    margin-bottom: 10px;
  }
}

@media screen and (max-width: 768px) {
  .dashboard {
    &__title {
      font-size: 20px;
    }
    
    &__date-range {
      display: block;
      margin-left: 0;
      margin-top: 5px;
    }
    
    &__pagination {
      text-align: center;
    }
  }
}
</style>
