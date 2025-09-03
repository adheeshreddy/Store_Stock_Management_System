  import React, { useState, useEffect } from "react";
import * as XLSX from "xlsx";
import api from "./api/axios";

export default function UploadProductsExcel() {
  const [file, setFile] = useState(null);
  const [productsToUpload, setProductsToUpload] = useState([]);
  const [existingProductsMap, setExistingProductsMap] = useState({});
  const [errors, setErrors] = useState({});
  const [uploading, setUploading] = useState(false);
  const [loadingExisting, setLoadingExisting] = useState(true);

  useEffect(() => {
    const loadExistingProducts = async () => {
      try {
        const res = await api.get("/product");
        const productMap = (res.data || []).reduce((map, p) => {
          map[p.id] = p.name;
          return map;
        }, {});
        setExistingProductsMap(productMap);
      } catch (e) {
        console.error("Failed to load existing products:", e);
      } finally {
        setLoadingExisting(false);
      }
    };
    loadExistingProducts();
  }, []);

  const validateProduct = (product) => {
    const productErrors = {};
    const normalizedId = product.id ? product.id.toString().trim() : '';

    if (normalizedId && existingProductsMap[normalizedId]) {
      productErrors.id = `Product ID '${normalizedId}' already exists.`;
    }

    if (!normalizedId) {
      productErrors.id = "Product ID cannot be empty.";
    }
    if (!product.name || product.name.toString().trim() === "") {
      productErrors.name = "Product Name cannot be empty.";
    } else if (product.name.toString().trim().length < 3) {
      productErrors.name = "Product Name must be at least 3 characters.";
    }
    if (!product.status || product.status.toString().trim() === "") {
      productErrors.status = "Status cannot be empty.";
    }
    return productErrors;
  };

  const handleFileChange = (e) => {
    const f = e.target.files[0];
    if (f) {
      setFile(f);
      const reader = new FileReader();
      reader.onload = (event) => {
        try {
          const data = new Uint8Array(event.target.result);
          const wb = XLSX.read(data, { type: "array" });
          const ws = wb.Sheets[wb.SheetNames[0]];
          const json = XLSX.utils.sheet_to_json(ws, { defval: "" });

          setProductsToUpload(json);
          const allErrors = {};
          let hasErrors = false;
          json.forEach((product, index) => {
            const productErrors = validateProduct(product);
            if (Object.keys(productErrors).length > 0) {
              allErrors[index] = productErrors;
              hasErrors = true;
            }
          });
          setErrors(allErrors);

          if (hasErrors) {
            alert("The Excel file contains validation errors. Please fix them before uploading.");
          }

        } catch (err) {
          console.error("Error reading file:", err);
          alert("Failed to read the Excel file. Please ensure it's a valid .xlsx or .xls file.");
        }
      };
      reader.readAsArrayBuffer(f);
    }
  };

  const handleUpload = async () => {
    if (productsToUpload.length === 0) {
      alert("Please select a valid Excel file first!");
      return;
    }

    if (Object.keys(errors).length > 0) {
      alert("Please fix the validation errors before uploading.");
      return;
    }

    setUploading(true);
    try {
      console.log(productsToUpload);
      const res = await api.post("/product/add", productsToUpload);
      alert(`${res.data.length} products uploaded successfully`);
      setProductsToUpload([]);
      setFile(null);
    } catch (err) {
      console.error(err);
      alert("Failed to upload products");
    } finally {
      setUploading(false);
    }
  };

  const handleDownloadTemplate = () => {
    const data = [
      ["id", "name", "status"],
      ["", "", ""],
      ["", "", ""]
    ];

    const ws = XLSX.utils.aoa_to_sheet(data);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Product Template");
    XLSX.writeFile(wb, "product_template.xlsx");
  };

  return (
    <div className="container mt-5">
      <h3 className="text-center fw-bold  mt-2">Bulk Upload Products</h3>
      <div>
        <button
              className="btn btn-primary btn-sm mb-3"
              style={{marginLeft:1200}}
              onClick={handleDownloadTemplate}
            >
              <i class="bi bi-download"></i> Template
            </button>
      </div>
      <div className="card p-4 shadow-sm">
        {loadingExisting ? (
          <div className="text-center">Loading existing product data...</div>
        ) : (
          <div className="mb-2 d-flex gap-3 align-items-center justify-content-center ">
            <input
              type="file"
              className="form-control me-2 w-25 "
              accept=".xlsx,.xls"
              onChange={handleFileChange}
            />
            <button
              className="btn btn-success "
              onClick={handleUpload}
              disabled={loadingExisting || uploading || Object.keys(errors).length > 0}
            >
              {uploading ? "Uploading..." : "Upload to DB"}
            </button>
          </div>

        )}

        {productsToUpload.length > 0 && (
          <div className="mt-3">
            <h5>Preview ({productsToUpload.length} products)</h5>
            <table className="table table-bordered table-hover mt-2">
              <thead className="table-light">
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Status</th>
                  <th>Validation Errors</th>
                </tr>
              </thead>
              <tbody>
                {productsToUpload.map((p, i) => {
                  const hasError = errors[i] && Object.keys(errors[i]).length > 0;
                  return (
                    <tr key={i} className={hasError ? "table-danger" : ""}>
                      <td>{p.id}</td>
                      <td>{p.name}</td>
                      <td>{p.status}</td>
                      <td>
                        {hasError && (
                          <ul className="list-unstyled mb-0 text-danger">
                            {Object.values(errors[i]).map((err, idx) => (
                              <li key={idx}>⚠ {err}</li>
                            ))}
                          </ul>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}