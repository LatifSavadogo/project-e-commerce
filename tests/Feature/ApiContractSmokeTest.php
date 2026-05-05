<?php

namespace Tests\Feature;

use Tests\TestCase;

class ApiContractSmokeTest extends TestCase
{
    public function test_csrf_endpoint_is_available(): void
    {
        $response = $this->getJson('/api/auth/csrf');
        $response->assertStatus(200)->assertJsonStructure(['token']);
    }

    public function test_protected_route_requires_authentication(): void
    {
        $response = $this->getJson('/api/v1/cart');
        $response->assertStatus(401);
    }
}
