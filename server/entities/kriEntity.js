// Mock data with comprehensive KRI fields
const mockKRIs = [
  {
    kriId: 'KRI-001',
    name: 'Credit Risk Exposure',
    kriDesc: 'Measures the total credit risk exposure across all portfolios',
    dataProvider: 'Risk Analytics Team',
    owner: 'John Smith',
    l1RiskType: 'Credit Risk',
    l2RiskType: 'Portfolio Concentration',
    reportCycle: 'Monthly',
    rasStatus: 'RAS',
    status: 'Active',
    lastUpdated: '2023-05-15',
    threshold: { yellow: 85, red: 95 },
    documents: [],
    workflowStatus: 'Approved'
  },
  {
    kriId: 'KRI-002',
    name: 'Liquidity Coverage Ratio',
    kriDesc: 'Measures the bank\'s ability to meet short-term obligations',
    dataProvider: 'Treasury Department',
    owner: 'Jane Doe',
    l1RiskType: 'Liquidity Risk',
    l2RiskType: 'Funding Stability',
    reportCycle: 'Weekly',
    rasStatus: 'RAS',
    status: 'Active',
    lastUpdated: '2023-05-10',
    threshold: { yellow: 110, red: 100 },
    documents: [],
    workflowStatus: 'Pending Review'
  },
  {
    kriId: 'KRI-003',
    name: 'Operational Loss Events',
    kriDesc: 'Tracks the number of operational loss events above threshold',
    dataProvider: 'Operational Risk Team',
    owner: 'Robert Johnson',
    l1RiskType: 'Operational Risk',
    l2RiskType: 'Process Execution',
    reportCycle: 'Monthly',
    rasStatus: 'Non-RAS',
    status: 'Active',
    lastUpdated: '2023-05-01',
    threshold: { yellow: 5, red: 10 },
    documents: [],
    workflowStatus: 'Draft'
  },
  {
    kriId: 'KRI-004',
    name: 'IT System Availability',
    kriDesc: 'Measures the availability of critical IT systems',
    dataProvider: 'IT Department',
    owner: 'Sarah Williams',
    l1RiskType: 'Technology Risk',
    l2RiskType: 'System Availability',
    reportCycle: 'Daily',
    rasStatus: 'RAS',
    status: 'Active',
    lastUpdated: '2023-05-14',
    threshold: { yellow: 99.5, red: 99 },
    documents: [],
    workflowStatus: 'Approved'
  },
  {
    kriId: 'KRI-005',
    name: 'Regulatory Compliance Breaches',
    kriDesc: 'Tracks the number of regulatory compliance breaches',
    dataProvider: 'Compliance Department',
    owner: 'Michael Brown',
    l1RiskType: 'Compliance Risk',
    l2RiskType: 'Regulatory Compliance',
    reportCycle: 'Monthly',
    rasStatus: 'RAS',
    status: 'Active',
    lastUpdated: '2023-04-30',
    threshold: { yellow: 1, red: 3 },
    documents: [],
    workflowStatus: 'Under Review'
  }
];

// Generate 45 more mock KRIs to meet the 50+ requirement
const riskTypes = ['Credit Risk', 'Market Risk', 'Operational Risk', 'Liquidity Risk', 'Compliance Risk', 'Technology Risk', 'Strategic Risk'];
const l2RiskTypes = {
  'Credit Risk': ['Default Risk', 'Concentration Risk', 'Country Risk', 'Settlement Risk'],
  'Market Risk': ['Interest Rate Risk', 'Foreign Exchange Risk', 'Commodity Risk', 'Equity Risk'],
  'Operational Risk': ['Process Risk', 'People Risk', 'System Risk', 'External Events'],
  'Liquidity Risk': ['Funding Risk', 'Market Liquidity Risk', 'Asset Liability Mismatch', 'Contingency Risk'],
  'Compliance Risk': ['Regulatory Risk', 'Legal Risk', 'Conduct Risk', 'Financial Crime Risk'],
  'Technology Risk': ['Cybersecurity Risk', 'Data Integrity Risk', 'System Availability Risk', 'IT Change Risk'],
  'Strategic Risk': ['Business Model Risk', 'Reputation Risk', 'Macroeconomic Risk', 'Competition Risk']
};
const reportCycles = ['Daily', 'Weekly', 'Monthly', 'Quarterly'];
const rasStatuses = ['RAS', 'Non-RAS'];
const workflowStatuses = ['Draft', 'Pending Review', 'Under Review', 'Approved', 'Rejected'];

for (let i = 6; i <= 50; i++) {
  const l1RiskType = riskTypes[Math.floor(Math.random() * riskTypes.length)];
  const l2Options = l2RiskTypes[l1RiskType];
  const l2RiskType = l2Options[Math.floor(Math.random() * l2Options.length)];

  mockKRIs.push({
    kriId: `KRI-${i.toString().padStart(3, '0')}`,
    name: `${l1RiskType} - ${l2RiskType} Indicator`,
    kriDesc: `Standard banking KRI for measuring ${l2RiskType.toLowerCase()} within ${l1RiskType.toLowerCase()}`,
    dataProvider: ['Risk Analytics Team', 'Treasury Department', 'Compliance Department', 'IT Department'][Math.floor(Math.random() * 4)],
    owner: ['John Smith', 'Jane Doe', 'Robert Johnson', 'Sarah Williams', 'Michael Brown'][Math.floor(Math.random() * 5)],
    l1RiskType,
    l2RiskType,
    reportCycle: reportCycles[Math.floor(Math.random() * reportCycles.length)],
    rasStatus: rasStatuses[Math.floor(Math.random() * rasStatuses.length)],
    status: 'Active',
    lastUpdated: `2023-${Math.floor(Math.random() * 4) + 1}-${Math.floor(Math.random() * 28) + 1}`,
    threshold: { yellow: Math.floor(Math.random() * 90) + 10, red: Math.floor(Math.random() * 90) + 10 },
    documents: [],
    workflowStatus: workflowStatuses[Math.floor(Math.random() * workflowStatuses.length)]
  });
}

// Mock data for KRI tasks
const mockKriTasks = [
  { id: 1, name: 'Z-KRI-0045-KRIV-0008', path: 'Global Financial Services > North America > Retail Banking', collectionStatus: 'Awaiting Collection', valueDate: null, value: null, breachStatus: 'Not Determined', hasTags: false, requiresAttention: true },
  { id: 2, name: 'Z-KRI-0045-KRIV-0007', path: 'Global Financial Services > North America > Retail Banking', collectionStatus: 'Awaiting Approval', valueDate: '11/14/2012', value: '0.50', breachStatus: 'Yellow', hasTags: true },
  { id: 3, name: 'Z-KRI-0045-KRIV-0006', path: 'Global Financial Services > North America > Retail Banking', collectionStatus: 'Collected', valueDate: '10/18/2012', value: '1.00', breachStatus: 'Yellow', hasTags: true },
  { id: 4, name: 'Z-KRI-0045-KRIV-0005', path: 'Global Financial Services > North America > Retail Banking', collectionStatus: 'Collected', valueDate: '9/12/2012', value: '1.00', breachStatus: 'Yellow', hasTags: true },
  { id: 5, name: 'Z-KRI-0045-KRIV-0004', path: 'Global Financial Services > North America > Retail Banking', collectionStatus: 'Collected', valueDate: '8/15/2012', value: '1.00', breachStatus: 'Yellow', hasTags: true },
  { id: 6, name: 'Z-KRI-0045-KRIV-0003', path: 'Global Financial Services > North America > Retail Banking', collectionStatus: 'Collected', valueDate: '7/12/2012', value: '1.00', breachStatus: 'Yellow', hasTags: true },
  { id: 7, name: 'Z-KRI-0045-KRIV-0002', path: 'Global Financial Services > North America > Retail Banking', collectionStatus: 'Collected', valueDate: '6/16/2012', value: '5.00', breachStatus: 'Red', hasTags: true },
  { id: 8, name: 'Z-KRI-0045-KRIV-0001', path: 'Global Financial Services > North America > Retail Banking', collectionStatus: 'Collected', valueDate: '5/16/2012', value: '1.00', breachStatus: 'Yellow', hasTags: true },
];

module.exports = {
  mockKRIs,
  mockKriTasks
};
