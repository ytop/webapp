# RDA Frontend Technical Specification

## 1. Overview

### 1.1 Technology Stack
- Framework: Vue.js
- UI Library: Element UI
- State Management: Vuex
- Charts: ECharts
- Build Tool: Webpack
- CSS Framework: Tailwind CSS

### 1.2 Architecture
The frontend follows a component-based architecture with:
- Vuex for centralized state management
- Vue Router for navigation
- Modular components for reusability
- Responsive design principles

## 2. Core Modules

### 2.1 Near Miss Monitoring Module

#### 2.1.1 Main Dashboard (`views/mra/nearMissMonitor/index.vue`)
Primary interface for near-miss monitoring with following components:

```vue
<template>
  <el-row :key="this.dataChanged">
    <el-collapse v-model="this.$store.getters.activeNames">
      <el-collapse-item name="1" title="Detection Charts">
        <!-- Detection visualization components -->
      </el-collapse-item>
    </el-collapse>
    <el-row>
      <NearMissTable v-if="this.$store.getters.tableName" 
                     :table-name="this.$store.getters.tableName" />
    </el-row>
  </el-row>
</template>
```

Components:
- Detection Charts Panel
- Near Miss Timeline
- Weekly Trend Analysis
- Detailed Data Table

#### 2.1.2 Charts and Visualizations

##### Bar Chart Component (`NearMissBarChart.vue`)
Weekly near-miss event visualization:

```javascript
{
  title: {
    text: 'Weekly Near Miss Event Count',
    x: 'center'
  },
  xAxis: {
    data: this.getX()  // Last 5 working days
  },
  yAxis: {
    minInterval: 1
  },
  series: [{
    type: 'bar',
    data: this.getData(),
    color: this.getColor()
  }]
}
```

##### Timeline Component (`NearMissMonitor.vue`)
Displays chronological near-miss events with:
- Event details
- Status indicators
- Department information
- Interactive links

### 2.2 Admin Module

#### 2.2.1 Admin Dashboard (`views/mra/adminIncident/index.vue`)
Administrative interface with tabs:
- Meta Data Management
- File Explorer
- Validation Tools
- Audit Trail

#### 2.2.2 Audit Trail Component (`AuditTrailData.vue`)
Comprehensive audit logging interface:
- Filterable data table
- Dynamic column rendering
- Export functionality
- CRUD operations

## 3. State Management

### 3.1 Vuex Store Structure

#### 3.1.1 Near Miss Store (`views/nearMiss.js`)
```javascript
const state = {
    tableName: '',
    detection_id: '',
    activeNames: ["1"],
    refresh: 0
}

const mutations = {
    changeTableName: (state, dept) => {
        state.tableName = dept
    },
    changeDetectionId: (state, id) => {
        state.detection_id = id
    },
    changeActiveNames: (state, an) => {
        state.activeNames = an
    },
    changeRefresh: (state) => {
        state.refresh += 1
    }
}
```

#### 3.1.2 Global Getters (`views/getters.js`)
```javascript
const getters = {
    token: state => state.user.token,
    permissions: state => state.user.permissions,
    userId: state => state.user.id,
    deptId: state => state.user.deptId
}
```

## 4. API Integration

### 4.1 Detection API
```javascript
export function getWeeklyDetections() {
    return request({
        url: '/mra/detection/weekly',
        method: 'get'
    })
}

export function getQuarterlyDetections() {
    return request({
        url: '/mra/detection/quarterly',
        method: 'get'
    })
}
```

### 4.2 Incident Management API
```javascript
export function getIncidentList(params = {}) {
    return request({
        url: '/mra/incident/list',
        method: 'get',
        params
    })
}
```

## 5. Component Design Guidelines

### 5.1 Reusable Components
- Table components with sorting/filtering
- Chart components with consistent styling
- Form components with validation
- Modal dialogs with standard actions

### 5.2 Style Guidelines
```css
/* Common card styling */
.near-miss-card {
    border-radius: 15px;
    box-shadow: 4px 4px 10px rgba(0, 0, 0, 0.2);
}

/* Chart container styling */
.chart-div {
    height: 100%;
    padding: 20px;
}

/* Interactive elements */
.detection-link {
    color: blue;
    text-decoration: underline;
    cursor: pointer;
}
```

## 6. Data Flow

### 6.1 Near Miss Detection Flow
1. User loads dashboard
2. Weekly/Quarterly data fetched
3. Charts and timeline updated
4. Real-time updates via refresh mechanism

### 6.2 Audit Trail Flow
1. Table metadata loaded
2. Dynamic columns rendered
3. Data fetched with pagination
4. CRUD operations with immediate feedback

## 7. Error Handling

### 7.1 API Error Handling
```javascript
try {
    const response = await getWeeklyDetections()
    if (!response.code || response.code !== 200) {
        throw new Error('Invalid response code')
    }
} catch (error) {
    this.$message({
        type: 'error',
        message: 'Failed to fetch detection data'
    })
}
```

### 7.2 Form Validation
- Required field validation
- Data type validation
- Custom business rule validation

## 8. Performance Optimization

### 8.1 Lazy Loading
- Route-level code splitting
- Conditional component rendering
- Dynamic import for large components

### 8.2 Caching Strategy
- API response caching
- Component state preservation
- Local storage utilization

## 9. Security Considerations

### 9.1 Authentication
- Token-based authentication
- Role-based access control
- Session management

### 9.2 Data Protection
- Input sanitization
- XSS prevention
- CSRF protection

## 10. Testing Strategy

### 10.1 Unit Testing
- Component testing
- Vuex store testing
- Utility function testing

### 10.2 Integration Testing
- API integration testing
- Component interaction testing
- End-to-end workflow testing

## 11. Deployment

### 11.1 Build Process
```bash
# Development
npm run dev

# Production
npm run build:prod
```

### 11.2 Environment Configuration
- Development
- Staging
- Production

## 12. Maintenance

### 12.1 Regular Tasks
- Dependency updates
- Performance monitoring
- Error logging and analysis

### 12.2 Documentation
- Component documentation
- API documentation
- State management documentation