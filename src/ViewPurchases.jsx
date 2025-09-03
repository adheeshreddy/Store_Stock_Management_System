import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "./api/axios";

export default function ViewPurchases() {
  const [headers, setHeaders] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);

  const [searchTerm, setSearchTerm] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const headersPerPage = 5;
  const navigate = useNavigate();

  useEffect(() => {
    const loadSuppliers = async () => {
      try {
        const res = await api.get("/suppliers");
        setSuppliers(res.data || []);
      } catch (e) {
        console.error("Failed suppliers", e);
      }
    };
    const loadProducts = async () => {
      try {
        const res = await api.get("/product", {
          headers: { "Content-Type": "text/plain" },
        });
        setProducts(res.data || []);
      } catch (e) {
        console.error("Failed products", e);
      }
    };
    loadSuppliers();
    loadProducts();
    loadHeaders();
  }, []);

  const loadHeaders = async () => {
    try {
      setLoading(true);
      const res = await api.get("/purchases");
      setHeaders(res.data || []);
    } catch {
      alert("Failed to load purchases");
    } finally {
      setLoading(false);
    }
  };

  const supplierMap = suppliers.reduce((map, s) => {
    map[s.id] = s.name;
    return map;
  }, {});

  const productMap = products.reduce((map, p) => {
    map[p.id] = p.name;
    return map;
  }, {});

  const filteredHeaders = headers.filter((h) => {
    if (!searchTerm) return true;

    const supplierName = supplierMap[h.supplierId] || "";
    const supplierMatch =
      h.supplierId.toString().includes(searchTerm) ||
      supplierName.toLowerCase().includes(searchTerm.toLowerCase());

    const productMatch = h.purchaseDetails?.some(
      (d) =>
        d.productId.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (productMap[d.productId] || "")
          .toLowerCase()
          .includes(searchTerm.toLowerCase())
    );

    return supplierMatch || productMatch;
  });

  const totalPages = Math.ceil(filteredHeaders.length / headersPerPage);
  const paginatedHeaders = filteredHeaders.slice(
    (currentPage - 1) * headersPerPage,
    currentPage * headersPerPage
  );

  const handlePageChange = (page) => {
    if (page >= 1 && page <= totalPages) setCurrentPage(page);
  };

  return (
    <div className="container mt-4">
      <h3 className="text-center fw-bold mb-3">Purchases</h3>

      <div className="d-flex justify-content-center mb-3">
        <input
          type="text"
          className="form-control"
          placeholder="Search by Supplier ID/Name "
          style={{ maxWidth: "400px" }}
          value={searchTerm}
          onChange={(e) => {
            setSearchTerm(e.target.value);
            setCurrentPage(1);
          }}
        />
      </div>

      {loading && <div className="text-muted">Loading...</div>}

      <div className="card p-3 shadow-sm">
        <table className="table table-bordered table-hover">
          <thead className="table-light text-center">
            <tr>
              <th>ID</th>
              <th>Supplier</th>
              <th>Taxable</th>
              <th>GST</th>
              <th>Total</th>
              <th>Created At</th>
              <th>Created By</th>
              <th>Open</th>
            </tr>
          </thead>
          <tbody className="text-center">
            {paginatedHeaders.length === 0 ? (
              <tr>
                <td colSpan={8} className="text-center text-muted">
                  No purchases
                </td>
              </tr>
            ) : (
              paginatedHeaders.map((h) => (
                <tr key={h.id}>
                  <td>{h.id}</td>
                  <td>
                    {supplierMap[h.supplierId] || "Unknown"} (ID: {h.supplierId})
                  </td>
                  <td>{Number(h.totalTaxableAmount).toFixed(2)}</td>
                  <td>{Number(h.totalGst).toFixed(2)}</td>
                  <td>{Number(h.totalAmount).toFixed(2)}</td>
                  <td>{h.createdAt}</td>
                  <td>{h.createdBy}</td>
                  <td>
                    <button
                      className="btn btn-sm btn-primary"
                      onClick={() => navigate(`/purchases/${h.id}`)}
                    >
                      Details
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        {filteredHeaders.length > headersPerPage && (
          <div className="d-flex justify-content-center align-items-center gap-2 mt-3">
            <button
              className="btn btn-outline-primary btn-sm"
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 1}
            >
              Prev
            </button>
            <span>
              Page {currentPage} of {totalPages}
            </span>
            <button
              className="btn btn-outline-primary btn-sm"
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages}
            >
              Next
            </button>
          </div>
        )}
      </div>
    </div>
  );
}