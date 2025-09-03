import React, { useState, useEffect } from "react";
import axios from "axios";
import Select from "react-select";
import api from "./api/axios";
import 'bootstrap/dist/css/bootstrap.min.css';

export default function AddSupplier() {
  const username = localStorage.getItem("username");
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    mobile: "",
    gender: "",
    country: "",
    state: "",
    city: "",
    address: "",
    createdBy: username,
  });

  const [errors, setErrors] = useState({});
  const [countries, setCountries] = useState([]);
  const [states, setStates] = useState([]);
  const [cities, setCities] = useState([]);
  const [successMessage, setSuccessMessage] = useState("");
  const [isSubmitDisabled, setIsSubmitDisabled] = useState(true);

  useEffect(() => {
    axios.get("https://countriesnow.space/api/v0.1/countries")
      .then(res => setCountries(res.data.data))
      .catch(err => console.error(err));
  }, []);

  useEffect(() => {
    if (!formData.country) {
      setStates([]);
      setFormData(prev => ({ ...prev, state: "", city: "" }));
      return;
    }
    axios.post("https://countriesnow.space/api/v0.1/countries/states", { country: formData.country })
      .then(res => setStates(res.data.data.states || []))
      .catch(err => console.error(err));
  }, [formData.country]);

  useEffect(() => {
    if (!formData.state) {
      setCities([]);
      setFormData(prev => ({ ...prev, city: "" }));
      return;
    }
    axios.post("https://countriesnow.space/api/v0.1/countries/state/cities", {
      country: formData.country,
      state: formData.state,
    })
      .then(res => setCities(res.data.data || []))
      .catch(err => console.error(err));
  }, [formData.state]);

  useEffect(() => {
    const newErrors = {};
    if (formData.name && !/^[A-Za-z\s]+$/.test(formData.name)) newErrors.name = "Invalid name";
    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) newErrors.email = "Invalid email";
    if (formData.mobile && !/^\d{10}$/.test(formData.mobile)) newErrors.mobile = "Mobile must be 10 digits";
    setErrors(newErrors);
    const isValid =
      !Object.keys(newErrors).length &&
      formData.name &&
      formData.email &&
      formData.mobile &&
      formData.gender &&
      formData.country &&
      formData.state &&
      formData.city &&
      formData.address.trim();
    setIsSubmitDisabled(!isValid);
  }, [formData]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === "mobile" ? value.replace(/\D/g, "") : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await api.post("/suppliers", formData);
      setSuccessMessage(`Supplier registered successfully! ID: ${res.data}`);
      setFormData({
        name: "",
        email: "",
        mobile: "",
        gender: "",
        country: "",
        state: "",
        city: "",
        address: "",
        createdBy: username,
      });
    } catch (err){
      setErrors({ form: err.response.data.details });
      console.log(err);
    }
  };

  const handleClear = () => {
     const confirmClear = window.confirm("Are you sure you want to clear the form?");
    if (confirmClear) {
       setFormData({
      name: "",
      email: "",
      mobile: "",
      gender: "",
      country: "",
      state: "",
      city: "",
      address: "",
      createdBy: username,
    });
    setErrors({});
    }
   
  };

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-lg-12">
          <h2 className="text-center mb-4">Register Supplier</h2>
          {errors.form && <div className="alert alert-danger">{errors.form}</div>}
          {successMessage && <div className="alert alert-success">{successMessage}</div>}
          <form onSubmit={handleSubmit} className="border rounded p-4 shadow-sm bg-white">
            <div className="row mb-3 g-3">
              <div className="col-md-6">
                <label className="form-label">Name:</label>
                <input
                  type="text"
                  className={`form-control ${errors.name ? "is-invalid" : ""}`}
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="Enter Supplier Name"
                />
                {errors.name && <div className="invalid-feedback">{errors.name}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Email:</label>
                <input
                  type="email"
                  className={`form-control ${errors.email ? "is-invalid" : ""}`}
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="Enter Supplier Email"
                />
                {errors.email && <div className="invalid-feedback">{errors.email}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Mobile:</label>
                <input
                  type="tel"
                  maxLength="10"
                  className={`form-control ${errors.mobile ? "is-invalid" : ""}`}
                  name="mobile"
                  value={formData.mobile}
                  onChange={handleChange}
                  placeholder="Enter Mobile Number"
                />
                {errors.mobile && <div className="invalid-feedback">{errors.mobile}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Gender:</label>
                <select
                  name="gender"
                  className="form-select"
                  value={formData.gender}
                  onChange={handleChange}
                >
                  <option value="">Select Gender</option>
                  <option value="M">Male</option>
                  <option value="F">Female</option>
                </select>
              </div>
            </div>

            <hr />

            <div className="row mb-3 g-3">
              <div className="col-md-4">
                <label className="form-label">Country:</label>
                <Select
                  options={countries.map(c => ({ label: c.country, value: c.country }))}
                  value={formData.country ? { label: formData.country, value: formData.country } : null}
                  onChange={(selected) =>
                    setFormData(prev => ({ ...prev, country: selected?.value || "", state: "", city: "" }))
                  }
                  isClearable
                  isSearchable
                  placeholder="Select Country"
                />
              </div>
              <div className="col-md-4">
                <label className="form-label">State:</label>
                <Select
                  options={states.map(s => ({ label: s.name || s, value: s.name || s }))}
                  value={formData.state ? { label: formData.state, value: formData.state } : null}
                  onChange={(selected) =>
                    setFormData(prev => ({ ...prev, state: selected?.value || "", city: "" }))
                  }
                  isClearable
                  isSearchable
                  placeholder="Select State"
                  isDisabled={!formData.country}
                />
              </div>
              <div className="col-md-4">
                <label className="form-label">City:</label>
                <Select
                  options={cities.map(c => ({ label: c, value: c }))}
                  value={formData.city ? { label: formData.city, value: formData.city } : null}
                  onChange={(selected) =>
                    setFormData(prev => ({ ...prev, city: selected?.value || "" }))
                  }
                  isClearable
                  isSearchable
                  placeholder="Select City"
                  isDisabled={!formData.state}
                />
              </div>
            </div>

            <hr />

            <div className="mb-3">
              <label className="form-label">Address:</label>
              <textarea
                name="address"
                className="form-control"
                value={formData.address}
                onChange={handleChange}
                placeholder="Enter Supplier Address"
              />
            </div>

            <div className="d-flex justify-content-center gap-3 mt-4">
              <button type="submit" className="btn btn-primary" disabled={isSubmitDisabled}>
                Submit
              </button>
              {/* {(formData.name || formData.email || formData.mobile || formData.gender || formData.country || formData.state || formData.city || formData.address) && (
                
              )} */}
              <button type="button" className="btn btn-danger" onClick={handleClear}>
                  Clear
                </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
/*
How to increase the expiration time upon user activity:
To extend the token's validity upon user activity (e.g., each successful request), you need to regenerate and send a new token to the client. This is often referred to as "token refreshing" or "sliding sessions."
Here's a common approach:
Modify JwtFilter to regenerate and send a new token:
Inside doFilterInternal, after successful token validation and setting the authentication in SecurityContextHolder, you can generate a new token using jwtUtil.generateToken(username).
Send this new token back to the client in a response header (e.g., Authorization header with "Bearer " prefix) or as part of the response body. The client-side application must then update its stored token with this new one.

    // Inside JwtFilter.java, within doFilterInternal
    if (jwtUtil.validateToken(token)) {
        String username = jwtUtil.extractUsername(token);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Regenerate and send new token
        String newToken = jwtUtil.generateToken(username);
        response.setHeader("Authorization", "Bearer " + newToken); // Or send in body
    }
    filterChain.doFilter(request, response);


    Client-side handling:
The client-side application (e.g., a web or mobile app) must be designed to receive this new token and replace the old one in its local storage or state. Subsequent requests will then use this newly extended token.
Considerations:
Security:
Ensure the new token is securely transmitted and stored on the client-side.
Performance:
Regenerating a token on every request can have a minor performance overhead, but for most applications, this is negligible.
Alternative: Refresh Tokens:
For more robust and secure token management, consider implementing a refresh token mechanism. This involves issuing a long-lived refresh token alongside a short-lived access token. When the access token expires, the client uses the refresh token to obtain a new access token without requiring re-authentication with username/password. This approach reduces the exposure of the access token.
*/