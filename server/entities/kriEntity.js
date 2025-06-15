
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
  mockKriTasks
};
