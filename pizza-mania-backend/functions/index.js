// functions/index.js
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const logger = require("firebase-functions/logger");

// Declare secret so Functions v2 injects it into process.env
const STRIPE_SECRET = defineSecret("STRIPE_SECRET");

exports.createPaymentIntent = onCall(
  {
    region: "asia-south1",
    secrets: [STRIPE_SECRET],
  },
  async (request) => {
    try {
      const data = request.data || {};
      const amountInt = Number.parseInt(data.amount, 10); // minor units (e.g., cents)
      const currency = String(data.currency || "").toLowerCase();

      if (!Number.isFinite(amountInt) || amountInt <= 0 || !currency) {
        throw new HttpsError(
          "invalid-argument",
          "Valid 'amount' (integer minor units) and 'currency' are required."
        );
      }

      // Read secret from env (Functions v2 injects it)
      const secret = String(process.env.STRIPE_SECRET || "").trim();
      if (!secret || !secret.startsWith("sk_")) {
        throw new HttpsError(
          "failed-precondition",
          "STRIPE_SECRET is missing or must start with 'sk_'. Set it in Secret Manager."
        );
      }

      // ESM-only stripe: dynamic import works under Node 20 + CommonJS
      const { default: Stripe } = await import("stripe");
      const stripe = new Stripe(secret, { apiVersion: "2024-06-20" });

      const pi = await stripe.paymentIntents.create({
        amount: amountInt,
        currency,
        description: "PizzaMania order",
        automatic_payment_methods: { enabled: true },
      });

      return { clientSecret: pi.client_secret, paymentIntentId: pi.id };
    } catch (err) {
      logger.error(err);
      if (err instanceof HttpsError) throw err;
      throw new HttpsError("internal", err.message || "Failed to create PaymentIntent");
    }
  }
);
