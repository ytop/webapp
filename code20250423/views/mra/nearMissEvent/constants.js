// constants.js

export const STATUS_ITEMS = [
  { name: 'Open', count: 10, type: 'success' },
  { name: 'Pending Validation', count: 0, type: 'success' },
  { name: 'Pending Owner Action', count: 0, type: 'success' },
  { name: 'Pending Closure Approval', count: 5, type: 'success' },
  { name: 'Closed', count: 135, type: 'success' }
]

export const SEARCH_FIELDS = [
  { label: 'Incident Number', value: 'incidentNo' },
  { label: 'Incident Type', value: 'incidentType' },
  { label: 'Status', value: 'status' },
  { label: 'Occurrence Date', value: 'occurrenceDate' },
  { label: 'Identification Date', value: 'identificationDate' },
  { label: 'Incident Title', value: 'incidentTitle' },
  { label: 'Identified By', value: 'identifiedBy' },
  { label: 'Owner Department', value: 'ownerDepartment' },
  { label: 'Description', value: 'description' },
  { label: 'Risk Rating', value: 'riskRating' },
  { label: 'Primary Risk Type', value: 'primaryRiskType' },
  { label: 'Primary Level II Risk', value: 'primaryLevelIIRisk' },
  { label: 'Secondary Risk Type', value: 'secondaryRiskType' },
  { label: 'Secondary Level II Risk', value: 'secondaryLevelIIRisk' },
  { label: 'Primary Owner Department', value: 'primaryOwnerDept' },
  { label: 'Secondary Owner Department', value: 'secondaryOwnerDept' }
]

export const DUE_DATE_OPTIONS = [
  { label: 'Past Due', value: 'pastDue' },
  { label: 'Due in 1 Month', value: 'upcomingMonth' },
  { label: 'Due in 1 Quarter', value: 'upcomingQuarter' },
  { label: 'Others', value: 'others' }
]

export const RISK_RATING_OPTIONS = [
  { label: 'Low', value: 'Low' },
  { label: 'Medium', value: 'Medium' },
  { label: 'High', value: 'High' }
]

export const OWNER_DEPT_OPTIONS = [
  { label: 'ADC', value: 'ADC' },
  { label: 'CLD', value: 'CLD' },
  { label: 'OSD', value: 'OSD' }
]


export const RISK_TYPE_OPTS = [
  { label: 'Operational Risk', value: 'Operational Risk' },
  { label: 'Technology and Information Security', value: 'Technology and Information Security' }
]

export const RISK_TYPE_OPTIONS = [
  {
    value: 'Compliance Risk',
    label: 'Compliance Risk',
    children: [
      {
        value: 'Anti-Bribery and Anti-Corruption',
        label: 'Anti-Bribery and Anti-Corruption'
      },
      { value: 'BSA/AML', label: 'BSA/AML' },
      {
        value: 'Consumer and Regulatory Compliance',
        label: 'Consumer and Regulatory Compliance'
      },
      { value: 'Sanctions', label: 'Sanctions' }
    ]
  },
  {
    value: 'Credit Risk',
    label: 'Credit Risk',
    children: [
      { value: 'Concentration', label: 'Concentration' },
      { value: 'Credit Default', label: 'Credit Default' },
      { value: 'Underwriting', label: 'Underwriting' },
      { value: 'Counterparty', label: 'Counterparty' }
    ]
  },
  {
    value: 'Interest Rate Risk',
    label: 'Interest Rate Risk',
    children: [
      { value: 'Basis', label: 'Basis' },
      { value: 'Options', label: 'Options' },
      { value: 'Repricing', label: 'Repricing' },
      { value: 'Yield Curve', label: 'Yield Curve' }
    ]
  },
  {
    value: 'Liquidity Risk',
    label: 'Liquidity Risk',
    children: [
      {
        value: 'Asset/Market Liquidity',
        label: 'Asset/Market Liquidity'
      },
      {
        value: 'Contingent Liquidity',
        label: 'Contingent Liquidity'
      },
      {
        value: 'Funding Liquidity',
        label: 'Funding Liquidity'
      },
      {
        value: 'Intraday Liquidity',
        label: 'Intraday Liquidity'
      }
    ]
  },
  {
    value: 'Operational Risk',
    label: 'Operational Risk',
    children: [
      { value: 'Fraud', label: 'Fraud' },
      { value: 'Human Capital', label: 'Human Capital' },
      { value: 'Legal', label: 'Legal' },
      { value: 'Model', label: 'Model' },
      {
        value: 'Physical Safety',
        label: 'Physical Safety'
      },
      { value: 'Reporting', label: 'Reporting' },
      { value: 'Third Party', label: 'Third Party' },
      { value: 'Execution', label: 'Execution' }
    ]
  },
  {
    value: 'Price Risk',
    label: 'Price Risk',
    children: [
      { value: 'Banking Book', label: 'Banking Book' },
      {
        value: 'Foreign Exchange',
        label: 'Foreign Exchange'
      },
      { value: 'Trading Book', label: 'Trading Book' }
    ]
  },
  {
    value: 'Reputational Risk',
    label: 'Reputational Risk',
    children: [
      {
        value: 'Brand Management',
        label: 'Brand Management'
      },
      {
        value: 'Relationship Management',
        label: 'Relationship Management'
      }
    ]
  },
  {
    value: 'Strategic Risk',
    label: 'Strategic Risk',
    children: [
      {
        value: 'Business Development',
        label: 'Business Development'
      },
      {
        value: 'Competitiveness',
        label: 'Competitiveness'
      },
      { value: 'ESG', label: 'ESG' },
      { value: 'Geopolitical', label: 'Geopolitical' },
      { value: 'Regulatory', label: 'Regulatory' }
    ]
  },
  {
    value: 'Technology and Information Security',
    label: 'Technology and Information Security',
    children: [
      { value: 'Software', label: 'Software' },
      { value: 'Hardware', label: 'Hardware' },
      { value: 'Systems', label: 'Systems' },
      { value: 'Cyber', label: 'Cyber' },
      { value: 'Innovation', label: 'Innovation' },
      {
        value: 'Physical Security',
        label: 'Physical Security'
      },
      { value: 'Data', label: 'Data' }
    ]
  }
]
