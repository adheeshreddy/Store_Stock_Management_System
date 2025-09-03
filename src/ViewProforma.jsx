import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import api from "./api/axios";

export default function ViewProforma() {
  const [headers, setHeaders] = useState([]);
  const [supplierMap, setSupplierMap] = useState({});
  const [searchTerm, setSearchTerm] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [headersPerPage, setHeadersPerPage] = useState(10);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    fetchSuppliers();
   fetchHeaders();
  }, [searchTerm, currentPage, headersPerPage]);

  const fetchSuppliers = async () => {
    try {
      const res = await api.get("/suppliers/all");
      if (res.data && typeof res.data === "object") {
        setSupplierMap(res.data);
      } else {
        console.error("Unexpected suppliers format", res.data);
      }
    } catch (err) {
      console.error(err);
      setError("Failed to fetch suppliers");
    }
  };

  const fetchHeaders = async () => {
    try {
      setLoading(true);
      setError("");
      const res = await api.get("/proformas");
      let allHeaders = [];
      if (Array.isArray(res.data)) {
        allHeaders = res.data;
      } else if (Array.isArray(res.data.content)) {
        allHeaders = res.data.content;
      } else {
        console.error("Unexpected proformas format", res.data);
        return;
      }

      const filtered = allHeaders.filter((h) => {
        const idMatch = h.supplierId.toString().includes(searchTerm);
        const supplierName = supplierMap[h.supplierId] || "";
        const supplierMatch = supplierName
          .toLowerCase()
          .includes(searchTerm.toLowerCase());
        return idMatch || supplierMatch;
      });

      const total = filtered.length;
      setTotalPages(Math.ceil(total / headersPerPage));
      const startIndex = (currentPage - 1) * headersPerPage;
      const endIndex = startIndex + headersPerPage;
      setHeaders(filtered.slice(startIndex, endIndex));
    } catch (err) {
      console.error(err);
      setError("Failed to fetch proforma headers");
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (id) => {
    if (!window.confirm("Are you sure you want to approve this proforma?")) return;
    try {
      await api.put(`/proformas/${id}/approve`);
      alert("Proforma approved successfully");
      fetchHeaders();
    } catch {
      alert("Failed to approve proforma");
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this proforma?")) return;
    try {
      await api.delete(`/proformas/${id}`);
      alert("Proforma deleted successfully");
      fetchHeaders();
    } catch {
      alert("Failed to delete proforma");
    }
  };

  const handleEdit = (id) => {
    navigate(`/edit/${id}`);
  };

  const handleDetails = (header) => {
    navigate(`/stockout/${header.id}`);
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    try {
      const date = new Date(dateString);
      return isNaN(date.getTime())
        ? dateString
        : date.toLocaleDateString("en-IN");
    } catch {
      return dateString;
    }
  };

  const getStatusBadge = (status) => {
    switch (status?.toLowerCase()) {
      case "a":
        return <span className="badge bg-success">APPROVED</span>;
      case "d":
        return <span className="badge bg-danger">DELETED</span>;
      case "e":
        return <span className="badge bg-warning">EDITED</span>;
      case "c":
        return <span className="badge bg-secondary">CREATED</span>;
      default:
        return (
          <span className="badge bg-dark">{status || "UNKNOWN"}</span>
        );
    }
  };

  const renderActions = (header) => {
    const status = header.status?.toLowerCase();
    if (status === "d") {
      return <span className="text-muted text-center">No Actions</span>;
    }
    if (status === "a") {
      return (
        <button
          className="btn btn-success btn-sm"
          onClick={() => handleDetails(header)}
        >
          Details
        </button>
      );
    }
    return (
      <div className="d-flex gap-2 justify-content-center">
        <button
          className="btn btn-primary btn-sm"
          onClick={() => handleEdit(header.id)}
        >
          Edit
        </button>
        <button
          className="btn btn-success btn-sm"
          onClick={() => handleApprove(header.id)}
        >
          Approve
        </button>
        <button
          className="btn btn-danger btn-sm"
          onClick={() => handleDelete(header.id)}
        >
          Delete
        </button>
      </div>
    );
  };

  const handlePageChange = (page) => {
    if (page < 1 || page > totalPages) return;
    setCurrentPage(page);
  };

  return (
    <>
      <div className="text-center mt-5">
        <h3 className="fw-bold ">Proforma Headers</h3>
        <div className="d-flex justify-content-center align-items-center">
          <label className="fw-bold mr-2 mt-4">Search:</label>
          <input
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="form-control mt-4"
            style={{ width: "250px", marginLeft: "10px" }}
            placeholder="Search by ID or Supplier"
          />
        </div>
      </div>

      <div className="d-flex align-items-center justify-content-center">
        <div
          className="bg-white p-4 rounded shadow"
          style={{ width: "90%", marginTop: "40px" }}
        >
          {loading ? (
            <div className="text-center">Loading...</div>
          ) : error ? (
            <div className="alert alert-danger">{error}</div>
          ) : headers.length === 0 ? (
            <div className="alert alert-info text-center">
              No proformas found.
            </div>
          ) : (
            <table className="table table-bordered">
              <thead className="text-center">
                <tr>
                  <th>ID</th>
                  <th>Supplier</th>
                  <th>Total Taxable</th>
                  <th>Total GST</th>
                  <th>Total Amount</th>
                  <th>Status</th>
                  <th>Created At</th>
                  <th>Created By</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody className="text-center">
                {headers.map((header) => (
                  <tr key={header.id}>
                    <td>{header.id}</td>
                    <td>
                      {header.supplierId} -{" "}
                      {supplierMap[header.supplierId] || "Unknown"}
                    </td>
                    <td>{header.totalTaxableAmount?.toFixed(2)}</td>
                    <td>{header.totalGst?.toFixed(2)}</td>
                    <td>{header.totalAmount?.toFixed(2)}</td>
                    <td>{getStatusBadge(header.status)}</td>
                    <td>{formatDate(header.createdAt)}</td>
                    <td>{header.createdBy}</td>
                    <td>{renderActions(header)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          <div className="d-flex justify-content-center align-items-center gap-4 mt-3">
            {currentPage > 1 && (
              <button
                className="btn btn-primary"
                onClick={() => handlePageChange(currentPage - 1)}
              >
                Previous
              </button>
            )}
            <span>
              Page {currentPage} of {totalPages}
            </span>
            {currentPage < totalPages && (
              <button
                className="btn btn-primary"
                onClick={() => handlePageChange(currentPage + 1)}
              >
                Next
              </button>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
