<template>
  <div class="near-miss-upload">
    <el-upload
      class="upload-demo"
      :action="false"
      :http-request="httpRequest"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      :before-upload="beforeUpload"
      multiple
      :limit="5"
      :on-exceed="handleExceed"
      :file-list="fileList">
        <el-button><el-icon name="upload2" /></el-button>
    </el-upload>
    
    <el-dialog :visible.sync="dialogVisible">
      <img width="100%" :src="dialogImageUrl" alt="">
    </el-dialog>
  </div>
</template>

<script>
import { uploadFiles } from '@/api/mra'
import XLSX from 'xlsx'

export default {
  name: 'UploadFile',
  props: ['taskIndicator'],
  data() {
    return {
      fileList: [],
      dialogImageUrl: '',
      dialogVisible: false
    };
  },
  methods: {
    handleRemove(file, fileList) {
      console.log(file, fileList);
    },
    handlePreview(file) {
      this.dialogImageUrl = file.url;
      this.dialogVisible = true;
    },
    handleExceed(files, fileList) {
      this.$message.warning(`You can only upload up to 5 files at once. 
        ${files.length} files selected. 
        ${files.length + fileList.length} files in total`);
    },

    
    // author: wentao xu
    // for excel format check
    beforeUpload(file) {
        if (process.env.VUE_APP_ENV === 'development') {
            console.log("This is DEV env, skip the file validation")
            return true
        }

      const isExceed1M = file.size / 1024 / 1024 > 1
      if (isExceed1M) {
        this.$message.warning('File size exceeds 1M')
        return false
      }

        if (this.taskIndicator.substring(3, this.taskIndicator.length) === '106') {
            if (!file.name.includes("QDII") && !file.name.includes("Recon") ) {
                this.$message.error("For 106, filename must include QDII or Recon.")
                return false;
            }
            return new Promise((resolve, reject) => {
                const reader = new FileReader();
                reader.onload = (e) => {
                    try {
                        const data = new Uint8Array(e.target.result);
                        const workbook = XLSX.read(data, {type: "array"});

                        if (!workbook.SheetNames.includes("Cover")) {
                            this.$message.error("Excel file lacks Cover sheet.")
                            return reject(false)
                        }
                        
                        const sheet = workbook.Sheets["Cover"]
                        const cellVal1 = sheet["A1"] ? sheet["A1"].v.trim() : "not exist"
                        const cellVal2 = sheet["A5"] ? sheet["A5"].v.trim() : "not exist"

                        if (cellVal1 === "BANK OF CHINA, NEW YORK BRANCH" && cellVal2 === "Reconciliation result Summary") {
                            return resolve(true)
                        } else {
                            this.$message.error("A1 and A5 in Cover sheet doesn't have reuqired value.")
                            return reject(false)
                        }
                    } catch (error) {
                        this.$message.error("Excel Analysis failed.")
                        return reject(false);
                    }
                }
                reader.readAsArrayBuffer(file)
            })
        }
        if (!file.name.includes("fed pos changes")) {
            this.$message.error("For 105, filename must include fed pos changes.")
            return false;
        }
        return new Promise((resolve, reject) => {
                const reader = new FileReader();
                reader.onload = (e) => {
                    try {
                        const data = new Uint8Array(e.target.result);
                        const workbook = XLSX.read(data, {type: "array"});

                        if (!workbook.SheetNames.includes("MOVE")) {
                            this.$message.error("Excel file lacks move sheet.")
                            return reject(false)
                        }

                        // check each column name
                        // author: wentao xu
                        const sheet = workbook.Sheets["MOVE"]
                        const cellVal1 = sheet["A1"] ? sheet["A1"].v : "not exist"
                        if (cellVal1 !== "CUSIP") {
                            this.$message.error("MOVE sheet lacks CUSIP")
                            return reject(false)
                        }
                        const cellVal2 = sheet["B1"] ? sheet["B1"].v : "not exist"
                        if (cellVal2 !== "BAL:") {
                            this.$message.error("MOVE sheet lacks BAL:")
                            return reject(false)
                        }
                        // const cellVal3 = sheet["C1"] ? sheet["C1"].v : "not exist"
                        const cellVal4 = sheet["D1"] ? sheet["D1"].v : "not exist"
                        if (cellVal4 !== "WDL:") {
                            this.$message.error("MOVE sheet lacks WDL:")
                            return reject(false)
                        }
                        const cellVal5 = sheet["E1"] ? sheet["E1"].v : "not exist"
                        if (cellVal5 !== "DEP:") {
                            this.$message.error("MOVE sheet lacks DEP:")
                            return reject(false)
                        }
                        // const cellVal6 = sheet["F1"] ? sheet["F1"].v : "not exist"
                        // const cellVal7 = sheet["G1"] ? sheet["G1"].v : "not exist"
                        const cellVal8 = sheet["H1"] ? sheet["H1"].v : "not exist"
                        if (cellVal8 !== "WDL-SELL") {
                            this.$message.error("MOVE sheet lacks WDL-SELL")
                            return reject(false)
                        }
                        const cellVal9 = sheet["I1"] ? sheet["I1"].v : "not exist"
                        if (cellVal9 !== "DEP+BUY") {
                            this.$message.error("MOVE sheet lacks DEP+BUY")
                            return reject(false)
                        }
                        const cellVal10 = sheet["J1"] ? sheet["J1"].v : "not exist"
                        if (cellVal10 !== "BAL-CLOSE") {
                            this.$message.error("MOVE sheet lacks BAL-CLOSE")
                            return reject(false)
                        }
                        // const cellVal11 = sheet["K1"] ? sheet["K1"].v : "not exist"
                        const cellVal12 = sheet["L1"] ? sheet["L1"].v : "not exist"
                        if (cellVal12 !== "CSDACCT") {
                            this.$message.error("MOVE sheet lacks CSDACCT")
                            return reject(false)
                        }
                        const cellVal13 = sheet["M1"] ? sheet["M1"].v : "not exist"
                        if (cellVal13 !== "CUSIP") {
                            this.$message.error("MOVE sheet lacks CUSIP")
                            return reject(false)
                        }
                        const cellVal14 = sheet["N1"] ? sheet["N1"].v : "not exist"
                        if (cellVal14 !== "ISIN") {
                            this.$message.error("MOVE sheet lacks ISIN")
                            return reject(false)
                        }
                        const cellVal15 = sheet["O1"] ? sheet["O1"].v : "not exist"
                        if (cellVal15 !== "Open") {
                            this.$message.error("MOVE sheet lacks Open")
                            return reject(false)
                        }
                        const cellVal16 = sheet["P1"] ? sheet["P1"].v : "not exist"
                        if (cellVal16 !== "Buy") {
                            this.$message.error("MOVE sheet lacks Buy")
                            return reject(false)
                        }
                        const cellVal17 = sheet["Q1"] ? sheet["Q1"].v : "not exist"
                        if (cellVal17 !== "Sell") {
                            this.$message.error("MOVE sheet lacks Sell")
                            return reject(false)
                        }
                        const cellVal18 = sheet["R1"] ? sheet["R1"].v : "not exist"
                        if (cellVal18 !== "Close") {
                            this.$message.error("MOVE sheet lacks Close")
                            return reject(false)
                        }

                        // ignore check number of rows
                        // const sheetData = XLSX.utils.sheet_to_json(sheet, { header: 1})
                        // const rowCount = sheetData.length
                        
                        // if (rowCount !== 94) {
                        //     this.$message.error("Number of rows doesn't match the requirements.")
                        //     return reject(false)
                        // }

                        // const columnA = sheetData.map(row => row[0]).filter(value => value !== undefined)
                        // const uniqueValues = new Set()
                        // ignore the duplicates check
                        // const duplicates = columnA.filter(value => {
                        //    if (uniqueValues.has(value)) {
                        //        this.$message.error(`Duplicates found in column A:${value}`)
                        //        return reject(false)
                        //    }
                        //    uniqueValues.add(value)
                        // })

                        
                        return resolve(true)


                    } catch (error) {
                        this.$message.error("Excel file analysis failed.")
                        return reject(false);
                    }
                }
                reader.readAsArrayBuffer(file)
            })
    },


    httpRequest(param) {
      const formData = new FormData()
      const _file = param.file
      formData.append('file', _file)
      return uploadFiles(formData)
        .then(response => {
          // Handle successful upload
          this.$message.success('File uploaded successfully')
          this.$store.commit('changeRefresh')
        })
        .catch(error => {
          // Handle upload error
          this.$message.error('Upload failed')
          this.$store.commit('changeRefresh')
        })
    }
  }
};
</script>

<style>
.upload-file {
  margin: 20px;
}
.el-upload__tip {
  color: #606266;
  margin-top: 7px;
}

.el-button:hover {
  color: #ffffff;
  background: rgb(136, 185, 249);
  border: #ffffff;
}
</style>
